import json
from base64 import b64encode
from collections.abc import AsyncIterator
from time import time
from typing import Any

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import ValidationError

from kuzhambu_workers.ai.image_generation import generate_image
from kuzhambu_workers.ai.openai_compatible import (
    invoke_chat_completion,
    iter_chat_completion_chunks,
)
from kuzhambu_workers.core.config import load_settings
from kuzhambu_workers.core.errors import WorkerError, protocol_failure
from kuzhambu_workers.core.security import verify_internal_request
from kuzhambu_workers.render.artifact_store import ArtifactMetadata, RequestArtifactStore
from kuzhambu_workers.schemas.ai import (
    AiInput,
    AiInvokeRequest,
    AiOptions,
    AiOutputSchema,
    AiPrompt,
)
from kuzhambu_workers.schemas.common import WorkerErrorPayload, WorkerStatus
from kuzhambu_workers.schemas.openai import (
    OpenAiCompatibleChatRequest,
    OpenAiCompatibleChatResponse,
    OpenAiCompatibleChoice,
    OpenAiCompatibleChoiceMessage,
    OpenAiCompatibleImageData,
    OpenAiCompatibleImageGenerationRequest,
    OpenAiCompatibleImageGenerationResponse,
    OpenAiCompatibleUsage,
    to_ai_image_model_config,
    to_ai_model_config,
)

router = APIRouter(prefix="/internal/openai/v1", tags=["OpenAI Compatible"])
OPENAI_COMPATIBLE_NOTICE = (
    "内部 OpenAI-compatible facade。请求形态贴近 OpenAI 接口，"
    "但必须携带 requestId、traceId 和 extendParams；workers 不保存供应商配置或 API Key。"
)


@router.post(
    "/chat-completions",
    response_model=None,
    summary="OpenAI-compatible chat completions",
    description=OPENAI_COMPATIBLE_NOTICE,
)
async def chat_completions(request: Request) -> JSONResponse | StreamingResponse:
    body = await request.body()
    parsed = _parse_openai_request(body)
    if isinstance(parsed, JSONResponse):
        return parsed

    auth_failure = _verify(request, body, parsed)
    if auth_failure is not None:
        return auth_failure

    ai_request = _to_ai_request(parsed)
    if isinstance(ai_request, JSONResponse):
        return ai_request
    if parsed.stream:
        return _stream_chat_completion(parsed, ai_request)
    return _invoke_chat_completion(parsed, ai_request)


@router.post(
    "/images/generations",
    response_model=None,
    summary="OpenAI-compatible image generations",
    description=OPENAI_COMPATIBLE_NOTICE,
)
async def image_generations(request: Request) -> JSONResponse:
    body = await request.body()
    parsed = _parse_image_request(body)
    if isinstance(parsed, JSONResponse):
        return parsed

    auth_failure = _verify(request, body, parsed)
    if auth_failure is not None:
        return auth_failure

    ai_request = _to_image_ai_request(parsed)
    if isinstance(ai_request, JSONResponse):
        return ai_request
    return _invoke_image_generation(parsed, ai_request)


def _parse_openai_request(body: bytes) -> OpenAiCompatibleChatRequest | JSONResponse:
    try:
        return OpenAiCompatibleChatRequest.model_validate_json(body)
    except ValidationError as exc:
        return _error_json(
            protocol_failure(
                "BAD_REQUEST",
                "OpenAI-compatible worker 请求体不合法。",
                detail={"errors": exc.errors(include_input=False)},
            ).to_payload(),
            400,
        )


def _parse_image_request(body: bytes) -> OpenAiCompatibleImageGenerationRequest | JSONResponse:
    try:
        return OpenAiCompatibleImageGenerationRequest.model_validate_json(body)
    except ValidationError as exc:
        return _error_json(
            protocol_failure(
                "BAD_REQUEST",
                "OpenAI-compatible 图片生成请求体不合法。",
                detail={"errors": exc.errors(include_input=False)},
            ).to_payload(),
            400,
        )


def _verify(
    request: Request,
    body: bytes,
    parsed: OpenAiCompatibleChatRequest | OpenAiCompatibleImageGenerationRequest,
) -> JSONResponse | None:
    try:
        verify_internal_request(
            method=request.method,
            path=request.url.path,
            headers=request.headers,
            body=body,
            settings=load_settings(),
            request_id=parsed.requestId,
            trace_id=parsed.traceId,
        )
    except WorkerError as exc:
        return _error_json(exc.to_payload(), _status_code(exc.code))
    return None


def _to_ai_request(request: OpenAiCompatibleChatRequest) -> AiInvokeRequest | JSONResponse:
    try:
        model_config = to_ai_model_config(request)
    except ValueError as exc:
        return _bad_model_config(exc)
    return AiInvokeRequest(
        requestId=request.requestId,
        traceId=request.traceId,
        callerDomain="AI",
        operation=request.operation,
        capability=request.capability,
        scope=request.scope,
        modelConfig=model_config,
        prompt=AiPrompt(messages=[message.model_dump(mode="json") for message in request.messages]),
        input=AiInput(contentType="OPENAI_COMPATIBLE_CHAT", payload={}),
        outputSchema=AiOutputSchema(type="json" if request.response_format else "text"),
        options=AiOptions(stream=request.stream, forceJson=request.response_format is not None),
    )


def _to_image_ai_request(
    request: OpenAiCompatibleImageGenerationRequest,
) -> AiInvokeRequest | JSONResponse:
    try:
        model_config = to_ai_image_model_config(request)
    except ValueError as exc:
        return _bad_model_config(exc)
    return AiInvokeRequest(
        requestId=request.requestId,
        traceId=request.traceId,
        callerDomain="AI",
        operation=request.operation,
        capability=request.capability,
        scope=request.scope,
        modelConfig=model_config,
        prompt=AiPrompt(messages=[{"role": "user", "content": request.prompt}]),
        input=AiInput(contentType="OPENAI_COMPATIBLE_IMAGE_GENERATION", payload={}),
        outputSchema=AiOutputSchema(type="artifact"),
        options=AiOptions(stream=False, forceJson=False),
    )


def _invoke_chat_completion(
    request: OpenAiCompatibleChatRequest,
    ai_request: AiInvokeRequest,
) -> JSONResponse:
    try:
        result = invoke_chat_completion(
            ai_request,
            response_format=request.response_format,
        )
    except Exception as exc:
        return _error_json(_worker_error_payload(exc), 502)

    response = OpenAiCompatibleChatResponse(
        id=request.requestId,
        created=int(time()),
        model=ai_request.modelConfig.modelName,
        choices=[
            OpenAiCompatibleChoice(
                index=0,
                message=OpenAiCompatibleChoiceMessage(content=result.content),
                finish_reason=result.raw_finish_reason,
            )
        ],
        usage=OpenAiCompatibleUsage(
            prompt_tokens=result.usage.inputTokens,
            completion_tokens=result.usage.outputTokens,
            total_tokens=_total_tokens(result.usage.inputTokens, result.usage.outputTokens),
        ),
    )
    return JSONResponse(response.model_dump(mode="json"))


def _invoke_image_generation(
    request: OpenAiCompatibleImageGenerationRequest,
    ai_request: AiInvokeRequest,
) -> JSONResponse:
    try:
        artifact = generate_image(ai_request)
    except Exception as exc:
        return _error_json(_worker_error_payload(exc), 502)

    if request.response_format == "b64_json":
        data = OpenAiCompatibleImageData(b64_json=b64encode(artifact.data).decode("ascii"))
    elif request.response_format == "url":
        metadata = _store_generated_image(
            request, artifact.data, artifact.content_type, artifact.filename
        )
        data = OpenAiCompatibleImageData(url=metadata.download_path)
    else:
        return _error_json(
            protocol_failure(
                "MODEL_CONFIG_INVALID",
                "OpenAI-compatible 图片生成当前仅支持 response_format=b64_json 或 url。",
            ).to_payload(),
            400,
        )
    response = OpenAiCompatibleImageGenerationResponse(created=int(time()), data=[data])
    return JSONResponse(response.model_dump(mode="json"))


def _store_generated_image(
    request: OpenAiCompatibleImageGenerationRequest,
    data: bytes,
    content_type: str,
    filename: str,
) -> ArtifactMetadata:
    settings = load_settings()
    store = RequestArtifactStore(
        request.requestId,
        settings.temp_dir,
        settings.artifact_chunk_bytes,
        settings.artifact_ttl_hours,
    )
    return store.put_bytes(
        data=data,
        format="ARTIFACT",
        filename=filename,
        content_type=content_type,
    )


def _stream_chat_completion(
    request: OpenAiCompatibleChatRequest,
    ai_request: AiInvokeRequest,
) -> StreamingResponse:
    async def events() -> AsyncIterator[str]:
        try:
            for chunk in iter_chat_completion_chunks(ai_request):
                if chunk.delta:
                    yield _openai_sse(
                        {
                            "id": request.requestId,
                            "object": "chat.completion.chunk",
                            "created": int(time()),
                            "model": ai_request.modelConfig.modelName,
                            "choices": [
                                {
                                    "index": 0,
                                    "delta": {"content": chunk.delta},
                                    "finish_reason": None,
                                }
                            ],
                        }
                    )
                if chunk.finish_reason is not None:
                    yield _openai_sse(
                        {
                            "id": request.requestId,
                            "object": "chat.completion.chunk",
                            "created": int(time()),
                            "model": ai_request.modelConfig.modelName,
                            "choices": [
                                {
                                    "index": 0,
                                    "delta": {},
                                    "finish_reason": chunk.finish_reason,
                                }
                            ],
                        }
                    )
            yield "data: [DONE]\n\n"
        except Exception as exc:
            yield _openai_sse({"error": _worker_error_payload(exc)})
            yield "data: [DONE]\n\n"

    return StreamingResponse(events(), media_type="text/event-stream")


def _openai_sse(payload: dict[str, Any]) -> str:
    data = json.dumps(payload, ensure_ascii=False, separators=(",", ":"))
    return f"data: {data}\n\n"


def _worker_error_payload(exc: Exception) -> dict[str, Any]:
    if isinstance(exc, WorkerError):
        return exc.to_payload()
    return protocol_failure(
        "WORKER_RESULT_UNEXPECTED",
        "OpenAI-compatible worker 结果处理失败。",
        detail={"errorClass": type(exc).__name__},
    ).to_payload()


def _bad_model_config(exc: ValueError) -> JSONResponse:
    return _error_json(
        protocol_failure(
            "MODEL_CONFIG_INVALID",
            str(exc),
            detail={"errorClass": type(exc).__name__},
        ).to_payload(),
        400,
    )


def _error_json(error: dict[str, Any], status_code: int) -> JSONResponse:
    payload = WorkerErrorPayload.model_validate(error)
    return JSONResponse(
        {
            "status": WorkerStatus.FAILED.value,
            "error": payload.model_dump(mode="json"),
            "errorType": payload.type,
            "errorMessage": payload.message,
        },
        status_code=status_code,
    )


def _status_code(code: str) -> int:
    if code in {"SERVICE_NOT_ALLOWED", "PATH_FORBIDDEN"}:
        return 403
    if code == "BAD_REQUEST":
        return 400
    return 401


def _total_tokens(input_tokens: int | None, output_tokens: int | None) -> int | None:
    if input_tokens is None or output_tokens is None:
        return None
    return input_tokens + output_tokens
