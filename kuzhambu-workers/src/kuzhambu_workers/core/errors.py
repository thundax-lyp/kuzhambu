from enum import Enum
from typing import Any

from kuzhambu_workers.core.logging import redact_data


class WorkerErrorType(str, Enum):
    WORKER_PROTOCOL_FAILURE = "WORKER_PROTOCOL_FAILURE"
    WORKER_TIMEOUT = "WORKER_TIMEOUT"
    WORKER_UNAVAILABLE = "WORKER_UNAVAILABLE"
    MODEL_TRANSPORT_FAILURE = "MODEL_TRANSPORT_FAILURE"
    MODEL_SEMANTIC_FAILURE = "MODEL_SEMANTIC_FAILURE"
    OUTPUT_FORMAT_FAILURE = "OUTPUT_FORMAT_FAILURE"
    IMAGE_INPUT_FAILURE = "IMAGE_INPUT_FAILURE"
    RENDER_INPUT_FAILURE = "RENDER_INPUT_FAILURE"
    RENDER_TEMPLATE_FAILURE = "RENDER_TEMPLATE_FAILURE"
    RENDER_OUTPUT_FAILURE = "RENDER_OUTPUT_FAILURE"
    UNSUPPORTED_CAPABILITY = "UNSUPPORTED_CAPABILITY"
    INTERNAL_FAILURE = "INTERNAL_FAILURE"


class WorkerError(Exception):
    def __init__(
        self,
        error_type: WorkerErrorType,
        code: str,
        message: str,
        *,
        retryable: bool = False,
        detail: dict[str, Any] | None = None,
    ) -> None:
        super().__init__(message)
        self.error_type = error_type
        self.code = code
        self.message = message
        self.retryable = retryable
        self.detail = detail or {}

    def to_payload(self) -> dict[str, Any]:
        return {
            "type": self.error_type,
            "code": self.code,
            "message": self.message,
            "retryable": self.retryable,
            "detail": redact_data(self.detail),
        }


def protocol_failure(code: str, message: str, detail: dict[str, Any] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.WORKER_PROTOCOL_FAILURE,
        code,
        message,
        detail=detail,
    )


def unsupported_capability(capability: str) -> WorkerError:
    return WorkerError(
        WorkerErrorType.UNSUPPORTED_CAPABILITY,
        "UNSUPPORTED_CAPABILITY",
        "workers 不支持请求的能力。",
        detail={"capability": capability},
    )


def internal_failure(error: Exception) -> WorkerError:
    return WorkerError(
        WorkerErrorType.INTERNAL_FAILURE,
        "INTERNAL_FAILURE",
        "worker 内部异常。",
        retryable=True,
        detail={"errorClass": type(error).__name__},
    )


def to_error_payload(error: Exception) -> dict[str, Any]:
    if isinstance(error, WorkerError):
        return error.to_payload()
    return internal_failure(error).to_payload()
