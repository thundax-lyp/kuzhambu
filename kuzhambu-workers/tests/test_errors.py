from kuzhambu_workers.core.errors import (
    WorkerError,
    internal_failure,
    protocol_failure,
    to_error_payload,
    unsupported_capability,
)
from kuzhambu_workers.core.logging import REDACTED
from kuzhambu_workers.schemas.common import WorkerErrorPayload, WorkerErrorType


def test_worker_error_payload_redacts_sensitive_detail() -> None:
    error = WorkerError(
        WorkerErrorType.MODEL_TRANSPORT_FAILURE,
        "MODEL_502",
        "模型服务调用失败。",
        retryable=True,
        detail={
            "apiKey": "model-secret",
            "prompt": "full prompt",
            "payload": {"text": "full payload"},
            "providerStatus": 502,
        },
    )

    payload = WorkerErrorPayload.model_validate(error.to_payload())

    assert payload.type == WorkerErrorType.MODEL_TRANSPORT_FAILURE
    assert payload.retryable is True
    assert payload.detail["apiKey"] == REDACTED
    assert payload.detail["prompt"] == REDACTED
    assert payload.detail["payload"] == REDACTED
    assert payload.detail["providerStatus"] == 502


def test_protocol_failure_uses_stable_type() -> None:
    payload = WorkerErrorPayload.model_validate(
        protocol_failure("BAD_REQUEST", "请求不合法。").to_payload()
    )

    assert payload.type == WorkerErrorType.WORKER_PROTOCOL_FAILURE
    assert payload.code == "BAD_REQUEST"
    assert payload.retryable is False


def test_unsupported_capability_uses_stable_type() -> None:
    payload = WorkerErrorPayload.model_validate(unsupported_capability("unknown").to_payload())

    assert payload.type == WorkerErrorType.UNSUPPORTED_CAPABILITY
    assert payload.detail == {"capability": "unknown"}


def test_unknown_exception_maps_to_internal_failure() -> None:
    payload = WorkerErrorPayload.model_validate(
        to_error_payload(ValueError("secret path /tmp/raw-prompt.txt"))
    )

    assert payload.type == WorkerErrorType.INTERNAL_FAILURE
    assert payload.code == "INTERNAL_FAILURE"
    assert payload.retryable is True
    assert payload.detail == {"errorClass": "ValueError"}


def test_internal_failure_does_not_expose_exception_message() -> None:
    payload = WorkerErrorPayload.model_validate(
        internal_failure(RuntimeError("full prompt leaked")).to_payload()
    )

    assert "full prompt leaked" not in payload.model_dump_json()
