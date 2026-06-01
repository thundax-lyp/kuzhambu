from dataclasses import dataclass
from typing import Any

from kuzhambu_workers.schemas.ai import AiModelConfig


@dataclass(frozen=True)
class ModelInvocation:
    model_name: str
    base_url: str
    parameters: dict[str, Any]
    timeout_ms: int
    supports_streaming: bool


def prepare_openai_compatible_invocation(model_config: AiModelConfig) -> ModelInvocation:
    return ModelInvocation(
        model_name=model_config.modelName,
        base_url=model_config.baseUrl,
        parameters=model_config.parameters,
        timeout_ms=model_config.timeoutMs,
        supports_streaming="streaming_text" in model_config.capabilityTags,
    )
