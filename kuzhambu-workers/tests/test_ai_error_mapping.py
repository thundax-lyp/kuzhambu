import pytest

from kuzhambu_workers.ai.errors import (
    model_config_invalid,
    model_provider_unavailable,
    model_rate_limited,
    model_request_rejected,
    model_timeout,
    model_transport_error,
    unsupported_model_api_source,
)
from kuzhambu_workers.ai.model_adapters import prepare_openai_compatible_invocation
from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType
from kuzhambu_workers.schemas.ai import AiModelConfig


def test_model_adapter_validates_openai_compatible_invocation() -> None:
    invocation = prepare_openai_compatible_invocation(_model_config())

    assert invocation.model_name == "model"
    assert invocation.base_url == "https://model.example/v1"
    assert invocation.chat_completions_url == "https://model.example/v1/chat/completions"
    assert invocation.parameters == {"temperature": 0.2}
    assert invocation.timeout_ms == 60000
    assert invocation.supports_streaming is True


def test_model_adapter_rejects_unsupported_api_source() -> None:
    config = _model_config(apiSource="LOCAL")

    with pytest.raises(WorkerError) as raised:
        prepare_openai_compatible_invocation(config)

    assert raised.value.error_type == WorkerErrorType.WORKER_PROTOCOL_FAILURE
    assert raised.value.code == "UNSUPPORTED_MODEL_API_SOURCE"
    assert raised.value.retryable is False


@pytest.mark.parametrize(
    ("field_name", "value"),
    [
        ("baseUrl", "ftp://model.example/v1"),
        ("baseUrl", ""),
        ("apiKey", ""),
        ("modelName", ""),
    ],
)
def test_model_adapter_rejects_invalid_required_config(field_name: str, value: str) -> None:
    config = _model_config(**{field_name: value})

    with pytest.raises(WorkerError) as raised:
        prepare_openai_compatible_invocation(config)

    assert raised.value.error_type == WorkerErrorType.WORKER_PROTOCOL_FAILURE
    assert raised.value.code == "MODEL_CONFIG_INVALID"
    assert raised.value.retryable is False


def test_model_adapter_rejects_reserved_provider_parameters() -> None:
    config = _model_config(parameters={"temperature": 0.2, "stream": True})

    with pytest.raises(WorkerError) as raised:
        prepare_openai_compatible_invocation(config)

    assert raised.value.code == "MODEL_CONFIG_INVALID"
    assert raised.value.detail["conflictFields"] == ["stream"]


def test_model_error_payload_redacts_sensitive_detail() -> None:
    error = model_config_invalid(
        "invalid",
        detail={
            "apiKey": "secret-key",
            "Authorization": "Bearer secret",
            "prompt": {"messages": ["raw"]},
            "modelName": "model",
        },
    )

    payload = error.to_payload()

    assert payload["detail"]["apiKey"] == "[REDACTED]"
    assert payload["detail"]["Authorization"] == "[REDACTED]"
    assert payload["detail"]["prompt"] == "[REDACTED]"
    assert payload["detail"]["modelName"] == "model"


@pytest.mark.parametrize(
    ("error", "expected_type", "expected_code", "expected_retryable"),
    [
        (
            unsupported_model_api_source(api_source="LOCAL", model_name="model"),
            WorkerErrorType.WORKER_PROTOCOL_FAILURE,
            "UNSUPPORTED_MODEL_API_SOURCE",
            False,
        ),
        (
            model_config_invalid("invalid"),
            WorkerErrorType.WORKER_PROTOCOL_FAILURE,
            "MODEL_CONFIG_INVALID",
            False,
        ),
        (
            model_transport_error("transport"),
            WorkerErrorType.MODEL_TRANSPORT_FAILURE,
            "MODEL_TRANSPORT_ERROR",
            True,
        ),
        (model_timeout(), WorkerErrorType.WORKER_TIMEOUT, "MODEL_TIMEOUT", True),
        (
            model_rate_limited(),
            WorkerErrorType.MODEL_TRANSPORT_FAILURE,
            "MODEL_RATE_LIMITED",
            True,
        ),
        (
            model_provider_unavailable(),
            WorkerErrorType.MODEL_TRANSPORT_FAILURE,
            "MODEL_PROVIDER_UNAVAILABLE",
            True,
        ),
        (
            model_request_rejected(),
            WorkerErrorType.MODEL_SEMANTIC_FAILURE,
            "MODEL_REQUEST_REJECTED",
            False,
        ),
    ],
)
def test_model_error_helpers_use_stable_error_contract(
    error: WorkerError,
    expected_type: WorkerErrorType,
    expected_code: str,
    expected_retryable: bool,
) -> None:
    assert error.error_type == expected_type
    assert error.code == expected_code
    assert error.retryable is expected_retryable


def _model_config(**overrides: object) -> AiModelConfig:
    payload = {
        "serviceRole": "PRIMARY",
        "apiSource": "OPENAI_COMPATIBLE",
        "baseUrl": "https://model.example/v1/",
        "apiKey": "process-only",
        "modelName": "model",
        "capabilityTags": ["text", "streaming_text"],
        "parameters": {"temperature": 0.2},
        "timeoutMs": 60000,
    }
    payload.update(overrides)
    return AiModelConfig.model_validate(payload)
