from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field

from kuzhambu_workers.schemas.ai import AiCapability, AiModelConfig, MessageRole

SUPPORTED_OPENAI_FACADE_API_SOURCES = frozenset({"OPENAI", "BYTEDANCE"})
OPENAI_COMPATIBLE_INTERNAL_API_SOURCE = "OPENAI_COMPATIBLE"


class OpenAiCompatibleMessage(BaseModel):
    role: MessageRole
    content: str


class OpenAiCompatibleChatRequest(BaseModel):
    model_config = ConfigDict(extra="allow")

    requestId: str
    traceId: str
    model: str
    messages: list[OpenAiCompatibleMessage]
    stream: bool = False
    extendParams: dict[str, Any] = Field(default_factory=dict)
    capability: AiCapability = AiCapability.ANSWER_GENERATION
    scope: str = "OPENAI_COMPATIBLE"
    operation: str = "OPENAI_COMPATIBLE_CHAT_COMPLETION"
    response_format: dict[str, Any] | None = None


class OpenAiCompatibleImageGenerationRequest(BaseModel):
    model_config = ConfigDict(extra="allow")

    requestId: str
    traceId: str
    model: str
    prompt: str
    extendParams: dict[str, Any] = Field(default_factory=dict)
    response_format: str = "b64_json"
    capability: AiCapability = AiCapability.IMAGE_GEN
    scope: str = "OPENAI_COMPATIBLE"
    operation: str = "OPENAI_COMPATIBLE_IMAGE_GENERATION"


class OpenAiCompatibleChoiceMessage(BaseModel):
    role: Literal["assistant"] = "assistant"
    content: str


class OpenAiCompatibleChoice(BaseModel):
    index: int
    message: OpenAiCompatibleChoiceMessage
    finish_reason: str | None = None


class OpenAiCompatibleUsage(BaseModel):
    prompt_tokens: int | None = None
    completion_tokens: int | None = None
    total_tokens: int | None = None


class OpenAiCompatibleChatResponse(BaseModel):
    id: str
    object: Literal["chat.completion"] = "chat.completion"
    created: int
    model: str
    choices: list[OpenAiCompatibleChoice]
    usage: OpenAiCompatibleUsage


class OpenAiCompatibleImageData(BaseModel):
    b64_json: str | None = None
    url: str | None = None


class OpenAiCompatibleImageGenerationResponse(BaseModel):
    created: int
    data: list[OpenAiCompatibleImageData]


def to_ai_model_config(request: OpenAiCompatibleChatRequest) -> AiModelConfig:
    api_source, real_model_name = split_openai_facade_model(request.model)
    base_url = _required_extend_param(request, "baseUrl")
    api_key = _required_extend_param(request, "apiKey")
    capability_tags = _string_list_extend_param(request, "capabilityTags")
    timeout_ms = _int_extend_param(request, "timeoutMs", 60_000)
    parameters = dict(request.model_extra or {})
    parameters.update(
        {
            key: value
            for key, value in request.extendParams.items()
            if key not in {"apiKey", "baseUrl", "capabilityTags", "timeoutMs"}
        }
    )
    return AiModelConfig(
        serviceRole=api_source,
        apiSource=OPENAI_COMPATIBLE_INTERNAL_API_SOURCE,
        baseUrl=base_url,
        apiKey=api_key,
        modelName=real_model_name,
        capabilityTags=capability_tags,
        parameters=parameters,
        timeoutMs=timeout_ms,
    )


def to_ai_image_model_config(request: OpenAiCompatibleImageGenerationRequest) -> AiModelConfig:
    api_source, real_model_name = split_openai_facade_model(request.model)
    base_url = _required_image_extend_param(request, "baseUrl")
    api_key = _required_image_extend_param(request, "apiKey")
    capability_tags = _string_list_image_extend_param(request, "capabilityTags")
    timeout_ms = _int_image_extend_param(request, "timeoutMs", 60_000)
    parameters = dict(request.model_extra or {})
    parameters.update(
        {
            key: value
            for key, value in request.extendParams.items()
            if key not in {"apiKey", "baseUrl", "capabilityTags", "timeoutMs"}
        }
    )
    parameters["response_format"] = request.response_format
    return AiModelConfig(
        serviceRole=api_source,
        apiSource=OPENAI_COMPATIBLE_INTERNAL_API_SOURCE,
        baseUrl=base_url,
        apiKey=api_key,
        modelName=real_model_name,
        capabilityTags=capability_tags,
        parameters=parameters,
        timeoutMs=timeout_ms,
    )


def split_openai_facade_model(model: str) -> tuple[str, str]:
    api_source, separator, real_model_name = model.partition("/")
    api_source = api_source.strip()
    real_model_name = real_model_name.strip()
    if not separator or not api_source or not real_model_name:
        raise ValueError("model 必须使用 {apiSource}/{realModelName} 格式。")
    if api_source not in SUPPORTED_OPENAI_FACADE_API_SOURCES:
        raise ValueError("model.apiSource 只支持 OPENAI 或 BYTEDANCE。")
    return api_source, real_model_name


def _required_extend_param(request: OpenAiCompatibleChatRequest, field_name: str) -> str:
    value = request.extendParams.get(field_name)
    if isinstance(value, str) and value.strip():
        return value.strip()
    raise ValueError(f"extendParams.{field_name} 不能为空。")


def _required_image_extend_param(
    request: OpenAiCompatibleImageGenerationRequest,
    field_name: str,
) -> str:
    value = request.extendParams.get(field_name)
    if isinstance(value, str) and value.strip():
        return value.strip()
    raise ValueError(f"extendParams.{field_name} 不能为空。")


def _string_list_extend_param(
    request: OpenAiCompatibleChatRequest,
    field_name: str,
) -> list[str]:
    value = request.extendParams.get(field_name)
    if value is None:
        return ["text"]
    if not isinstance(value, list):
        raise ValueError(f"extendParams.{field_name} 必须是字符串数组。")
    result = [item.strip() for item in value if isinstance(item, str) and item.strip()]
    if len(result) != len(value):
        raise ValueError(f"extendParams.{field_name} 必须是字符串数组。")
    return result


def _string_list_image_extend_param(
    request: OpenAiCompatibleImageGenerationRequest,
    field_name: str,
) -> list[str]:
    value = request.extendParams.get(field_name)
    if value is None:
        return ["image_gen"]
    if not isinstance(value, list):
        raise ValueError(f"extendParams.{field_name} 必须是字符串数组。")
    result = [item.strip() for item in value if isinstance(item, str) and item.strip()]
    if len(result) != len(value):
        raise ValueError(f"extendParams.{field_name} 必须是字符串数组。")
    return result


def _int_extend_param(
    request: OpenAiCompatibleChatRequest,
    field_name: str,
    default: int,
) -> int:
    value = request.extendParams.get(field_name)
    if value is None:
        return default
    if isinstance(value, int):
        return value
    raise ValueError(f"extendParams.{field_name} 必须是整数。")


def _int_image_extend_param(
    request: OpenAiCompatibleImageGenerationRequest,
    field_name: str,
    default: int,
) -> int:
    value = request.extendParams.get(field_name)
    if value is None:
        return default
    if isinstance(value, int):
        return value
    raise ValueError(f"extendParams.{field_name} 必须是整数。")
