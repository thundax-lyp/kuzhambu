from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse, StreamingResponse

from kuzhambu_workers.ai.usecase_registry import USECASES, AiUsecase, AiUsecaseDomain
from kuzhambu_workers.api.ai_routes import (
    AiRequestValidator,
    invoke_ai_request,
    stream_ai_request,
)
from kuzhambu_workers.core.errors import protocol_failure
from kuzhambu_workers.schemas.ai import AiInvokeRequest
from kuzhambu_workers.schemas.common import WorkerErrorPayload

router = APIRouter()
_DOMAIN_TAGS = {
    AiUsecaseDomain.CLASSICS: "Classics",
    AiUsecaseDomain.DISCOVERY: "Discovery",
    AiUsecaseDomain.KNOWLEDGE: "Knowledge",
    AiUsecaseDomain.PLATFORM: "Platform",
}


def _register_usecase(usecase: AiUsecase) -> None:
    async def endpoint(request: Request) -> JSONResponse | StreamingResponse:
        if usecase.stream:
            return await stream_ai_request(request, validate=_validator(usecase))
        return await invoke_ai_request(request, validate=_validator(usecase))

    router.add_api_route(
        usecase.path,
        endpoint,
        methods=["POST"],
        response_model=None,
        summary=usecase.summary,
        description=_description(usecase),
        name=usecase.operation.lower(),
        tags=[_DOMAIN_TAGS[usecase.domain]],
    )


def _validator(usecase: AiUsecase) -> AiRequestValidator:
    def validate(request: AiInvokeRequest) -> WorkerErrorPayload | None:
        if request.capability != usecase.capability:
            return WorkerErrorPayload.model_validate(
                protocol_failure(
                    "BAD_REQUEST",
                    "AI usecase path 与 capability 不匹配。",
                    detail={
                        "path": usecase.path,
                        "expectedCapability": usecase.capability.value,
                        "actualCapability": request.capability.value,
                    },
                ).to_payload()
            )
        if request.options.stream != usecase.stream:
            return WorkerErrorPayload.model_validate(
                protocol_failure(
                    "BAD_REQUEST",
                    "AI usecase path 与 stream 选项不匹配。",
                    detail={
                        "path": usecase.path,
                        "expectedStream": usecase.stream,
                        "actualStream": request.options.stream,
                    },
                ).to_payload()
            )
        return None

    return validate


def _description(usecase: AiUsecase) -> str:
    stream_mode = "SSE 流式响应" if usecase.stream else "同步 JSON 响应"
    return (
        f"{usecase.description}\n\n"
        f"调用方固定为 kuzhambu-ai；capability 必须为 `{usecase.capability.value}`；"
        f"options.stream 必须为 `{str(usecase.stream).lower()}`；响应模式为 {stream_mode}；"
        "最终态字段固定包含 status、usage、failureStage、fallbackUsed、errorType、"
        "errorMessage 和 result 或 artifactReference。"
        "批量场景按 requestId + traceId 识别单元最终态；单元失败只影响当前单元，"
        "上游可直接基于 failureStage、errorType、errorMessage 聚合失败摘要。"
    )


_ENABLED_DOMAINS = {
    AiUsecaseDomain.CLASSICS,
    AiUsecaseDomain.DISCOVERY,
    AiUsecaseDomain.KNOWLEDGE,
    AiUsecaseDomain.PLATFORM,
}


for _usecase in USECASES:
    if _usecase.domain in _ENABLED_DOMAINS:
        _register_usecase(_usecase)
