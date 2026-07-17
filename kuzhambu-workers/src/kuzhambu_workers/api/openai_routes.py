import json
from base64 import b64encode
from collections.abc import AsyncIterator
from time import time
from typing import Any

from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse
from pydantic import ValidationError

from kuzhambu_workers.ai.graph_registry import GraphRegistry
from kuzhambu_workers.core.config import load_settings
from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType, protocol_failure
from kuzhambu_workers.core.security import verify_internal_request
from kuzhambu_workers.render.artifact_store import ArtifactMetadata, RequestArtifactStore
from kuzhambu_workers.schemas.ai import (
    AiCapability,
    AiInput,
    AiInvokeRequest,
    AiOptions,
    AiOutputSchema,
    AiPrompt,
    AiResult,
    ResultFormat,
)
from kuzhambu_workers.schemas.common import UsageSummary, WorkerErrorPayload, WorkerStatus
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
_REGISTRY = GraphRegistry.build_default()
SUPPORTED_IMAGE_RESPONSE_FORMATS = frozenset({"b64_json", "url"})
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
    validation_failure = _validate_chat_completion_request(parsed, ai_request)
    if validation_failure is not None:
        return validation_failure
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
    validation_failure = _validate_image_generation_request(parsed, ai_request)
    if validation_failure is not None:
        return validation_failure
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
        input=AiInput(
            contentType="OPENAI_COMPATIBLE_CHAT",
            payload=_chat_input_payload(request),
        ),
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


def _chat_input_payload(request: OpenAiCompatibleChatRequest) -> dict[str, Any]:
    if request.response_format is None:
        return {}
    return {"responseFormat": request.response_format}


def _validate_chat_completion_request(
    request: OpenAiCompatibleChatRequest,
    ai_request: AiInvokeRequest,
) -> JSONResponse | None:
    if (
        request.operation != "OPENAI_COMPATIBLE_CHAT_COMPLETION"
        or request.capability == AiCapability.IMAGE_GEN
    ):
        return _error_json(
            protocol_failure(
                "MODEL_CONFIG_INVALID",
                "OpenAI-compatible chat completion 接口只能路由到 chat graph。",
                detail={
                    "capability": request.capability.value,
                    "operation": request.operation,
                },
            ).to_payload(),
            400,
        )
    requested_count = _effective_int_parameter(ai_request.modelConfig.parameters, "n", 1)
    if requested_count != 1:
        return _error_json(
            protocol_failure(
                "MODEL_CONFIG_INVALID",
                "OpenAI-compatible chat completion 当前仅支持 n=1。",
                detail={"n": requested_count},
            ).to_payload(),
            400,
        )
    return None


def _validate_image_generation_request(
    request: OpenAiCompatibleImageGenerationRequest,
    ai_request: AiInvokeRequest,
) -> JSONResponse | None:
    if request.response_format not in SUPPORTED_IMAGE_RESPONSE_FORMATS:
        return _error_json(
            protocol_failure(
                "MODEL_CONFIG_INVALID",
                "OpenAI-compatible 图片生成当前仅支持 response_format=b64_json 或 url。",
            ).to_payload(),
            400,
        )
    if (
        request.capability != AiCapability.IMAGE_GEN
        or request.operation != "OPENAI_COMPATIBLE_IMAGE_GENERATION"
    ):
        return _error_json(
            protocol_failure(
                "MODEL_CONFIG_INVALID",
                "OpenAI-compatible 图片生成接口只能路由到 image generation graph。",
                detail={
                    "capability": request.capability.value,
                    "operation": request.operation,
                },
            ).to_payload(),
            400,
        )
    parameters = ai_request.modelConfig.parameters
    requested_count = _effective_int_parameter(parameters, "n", 1)
    if requested_count != 1:
        return _error_json(
            protocol_failure(
                "MODEL_CONFIG_INVALID",
                "OpenAI-compatible 图片生成当前仅支持 n=1。",
                detail={"n": requested_count},
            ).to_payload(),
            400,
        )
    stream = parameters.get("stream")
    if stream is not None and stream is not False:
        return _error_json(
            protocol_failure(
                "MODEL_CONFIG_INVALID",
                "OpenAI-compatible 图片生成同步接口不支持 stream=true。",
                detail={"stream": stream},
            ).to_payload(),
            400,
        )
    return None


def _effective_int_parameter(parameters: dict[str, Any], field_name: str, default: int) -> int:
    value = parameters.get(field_name, default)
    if value is None:
        return default
    if type(value) is int:
        return value
    return -1


def _invoke_chat_completion(
    request: OpenAiCompatibleChatRequest,
    ai_request: AiInvokeRequest,
) -> JSONResponse:
    try:
        graph_result = _REGISTRY.invoke(ai_request)
        result = AiResult.model_validate(graph_result)
    except Exception as exc:
        return _error_json(_worker_error_payload(exc), 502)

    response = OpenAiCompatibleChatResponse(
        id=request.requestId,
        created=int(time()),
        model=ai_request.modelConfig.modelName,
        choices=[
            OpenAiCompatibleChoice(
                index=0,
                message=OpenAiCompatibleChoiceMessage(content=_text_payload(result)),
                finish_reason=_raw_finish_reason(graph_result),
            )
        ],
        usage=_openai_usage_from_graph_result(graph_result),
    )
    return JSONResponse(response.model_dump(mode="json"))


def _invoke_image_generation(
    request: OpenAiCompatibleImageGenerationRequest,
    ai_request: AiInvokeRequest,
) -> JSONResponse:
    try:
        graph_result = _REGISTRY.invoke(ai_request)
        result = AiResult.model_validate(graph_result)
    except Exception as exc:
        return _error_json(_worker_error_payload(exc), 502)
    try:
        artifact = _image_artifact_payload(result)
        if request.response_format == "b64_json":
            data = OpenAiCompatibleImageData(b64_json=b64encode(artifact["data"]).decode("ascii"))
        elif request.response_format == "url":
            metadata = _store_generated_image(
                request,
                artifact["data"],
                artifact["contentType"],
                artifact["filename"],
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
    except Exception as exc:
        return _error_json(_worker_error_payload(exc), 502)
    response = OpenAiCompatibleImageGenerationResponse(created=int(time()), data=[data])
    return JSONResponse(response.model_dump(mode="json"))


def _text_payload(result: AiResult) -> str:
    if result.format != ResultFormat.TEXT:
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "OpenAI-compatible chat facade 仅支持 TEXT 结果。",
        )
    if not isinstance(result.payload, str):
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "OpenAI-compatible chat facade TEXT 结果必须是字符串。",
        )
    return result.payload


def _image_artifact_payload(result: AiResult) -> dict[str, Any]:
    if result.format != ResultFormat.ARTIFACT:
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "OpenAI-compatible 图片生成结果必须是 ARTIFACT。",
        )
    if not isinstance(result.payload, dict):
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "OpenAI-compatible 图片生成结果 payload 不合法。",
        )
    data = result.payload.get("data")
    content_type = result.payload.get("contentType")
    filename = result.payload.get("filename")
    if not isinstance(data, bytes):
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "OpenAI-compatible 图片生成结果缺少图片 bytes。",
        )
    if not isinstance(content_type, str) or not content_type.strip():
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "OpenAI-compatible 图片生成结果缺少 contentType。",
        )
    if not isinstance(filename, str) or not filename.strip():
        raise protocol_failure(
            "WORKER_RESULT_INVALID",
            "OpenAI-compatible 图片生成结果缺少 filename。",
        )
    return {"data": data, "contentType": content_type, "filename": filename}


def _raw_finish_reason(result: dict[str, Any]) -> str | None:
    finish_reason = result.get("rawFinishReason")
    if isinstance(finish_reason, str):
        return finish_reason
    return None


def _openai_usage_from_graph_result(result: dict[str, Any]) -> OpenAiCompatibleUsage:
    usage = result.get("usage")
    if not isinstance(usage, dict):
        return OpenAiCompatibleUsage()
    input_tokens = usage.get("inputTokens")
    output_tokens = usage.get("outputTokens")
    return OpenAiCompatibleUsage(
        prompt_tokens=input_tokens if isinstance(input_tokens, int) else None,
        completion_tokens=output_tokens if isinstance(output_tokens, int) else None,
        total_tokens=_total_tokens(
            input_tokens if isinstance(input_tokens, int) else None,
            output_tokens if isinstance(output_tokens, int) else None,
        ),
    )


def _openai_usage_from_summary(usage: UsageSummary) -> OpenAiCompatibleUsage:
    return OpenAiCompatibleUsage(
        prompt_tokens=usage.inputTokens,
        completion_tokens=usage.outputTokens,
        total_tokens=_total_tokens(usage.inputTokens, usage.outputTokens),
    )


def _store_generated_image(
    request: OpenAiCompatibleImageGenerationRequest,
    data: bytes,
    content_type: str,
    filename: str,
) -> ArtifactMetadata:
    settings = load_settings()
    if len(data) > settings.max_artifact_bytes:
        raise WorkerError(
            WorkerErrorType.IMAGE_INPUT_FAILURE,
            "IMAGE_ARTIFACT_TOO_LARGE",
            "图片生成结果超过 workers artifact 大小限制。",
            detail={
                "sizeBytes": len(data),
                "maxArtifactBytes": settings.max_artifact_bytes,
            },
        )
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
            include_usage = _include_stream_usage(ai_request)
            for chunk in _REGISTRY.stream_chat_completion(
                ai_request,
                response_format=request.response_format,
            ):
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
                if include_usage and chunk.provider_usage and chunk.usage is not None:
                    yield _openai_sse(
                        {
                            "id": request.requestId,
                            "object": "chat.completion.chunk",
                            "created": int(time()),
                            "model": ai_request.modelConfig.modelName,
                            "choices": [],
                            "usage": _openai_usage_from_summary(chunk.usage).model_dump(
                                mode="json"
                            ),
                        }
                    )
            yield "data: [DONE]\n\n"
        except Exception as exc:
            yield _openai_sse({"error": _worker_error_payload(exc)})
            yield "data: [DONE]\n\n"

    return StreamingResponse(events(), media_type="text/event-stream")


def _include_stream_usage(ai_request: AiInvokeRequest) -> bool:
    stream_options = ai_request.modelConfig.parameters.get("stream_options")
    if not isinstance(stream_options, dict):
        return False
    return stream_options.get("include_usage") is True


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
