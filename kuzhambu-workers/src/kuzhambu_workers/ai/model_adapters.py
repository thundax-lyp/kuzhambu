from dataclasses import dataclass
from typing import Any
from urllib.parse import urlparse

from kuzhambu_workers.ai.errors import model_config_invalid, unsupported_model_api_source
from kuzhambu_workers.schemas.ai import AiModelConfig

OPENAI_COMPATIBLE_API_SOURCE = "OPENAI_COMPATIBLE"
OPENAI_CHAT_COMPLETIONS_PATH = "/chat/completions"
RESERVED_PROVIDER_PARAMETERS = frozenset(
    {
        "messages",
        "model",
        "response_format",
        "stream",
    }
)


@dataclass(frozen=True)
class ModelInvocation:
    model_name: str
    base_url: str
    chat_completions_url: str
    parameters: dict[str, Any]
    timeout_ms: int
    supports_streaming: bool


def prepare_openai_compatible_invocation(model_config: AiModelConfig) -> ModelInvocation:
    _validate_api_source(model_config)
    base_url = _validated_base_url(model_config)
    model_name = _validated_required_field("modelName", model_config.modelName)
    _validated_required_field("apiKey", model_config.apiKey)
    parameters = _validated_parameters(model_config)
    timeout_ms = _validated_timeout_ms(model_config)
    return ModelInvocation(
        model_name=model_name,
        base_url=base_url,
        chat_completions_url=f"{base_url}{OPENAI_CHAT_COMPLETIONS_PATH}",
        parameters=parameters,
        timeout_ms=timeout_ms,
        supports_streaming="streaming_text" in model_config.capabilityTags,
    )


def _validate_api_source(model_config: AiModelConfig) -> None:
    if model_config.apiSource == OPENAI_COMPATIBLE_API_SOURCE:
        return
    raise unsupported_model_api_source(
        api_source=model_config.apiSource,
        model_name=model_config.modelName,
    )


def _validated_base_url(model_config: AiModelConfig) -> str:
    base_url = _validated_required_field("baseUrl", model_config.baseUrl).rstrip("/")
    parsed = urlparse(base_url)
    if parsed.scheme not in {"http", "https"} or not parsed.netloc:
        raise model_config_invalid(
            "modelConfig.baseUrl 必须是 HTTP 或 HTTPS URL。",
            detail={"field": "baseUrl", "modelName": model_config.modelName},
        )
    return base_url


def _validated_required_field(field_name: str, value: str) -> str:
    stripped = value.strip()
    if stripped:
        return stripped
    raise model_config_invalid(
        f"modelConfig.{field_name} 不能为空。",
        detail={"field": field_name},
    )


def _validated_parameters(model_config: AiModelConfig) -> dict[str, Any]:
    conflicts = sorted(set(model_config.parameters).intersection(RESERVED_PROVIDER_PARAMETERS))
    if conflicts:
        raise model_config_invalid(
            "modelConfig.parameters 不得覆盖 provider payload 核心字段。",
            detail={
                "conflictFields": conflicts,
                "modelName": model_config.modelName,
            },
        )
    return dict(model_config.parameters)


def _validated_timeout_ms(model_config: AiModelConfig) -> int:
    if model_config.timeoutMs > 0:
        return model_config.timeoutMs
    raise model_config_invalid(
        "modelConfig.timeoutMs 必须大于 0。",
        detail={"field": "timeoutMs", "modelName": model_config.modelName},
    )
