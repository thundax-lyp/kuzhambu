from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType


def unsupported_model_api_source(*, api_source: str, model_name: str) -> WorkerError:
    return WorkerError(
        WorkerErrorType.WORKER_PROTOCOL_FAILURE,
        "UNSUPPORTED_MODEL_API_SOURCE",
        "workers 仅支持 OpenAI-compatible 模型接口。",
        detail={
            "apiSource": api_source,
            "modelName": model_name,
        },
    )


def model_config_invalid(message: str, *, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.WORKER_PROTOCOL_FAILURE,
        "MODEL_CONFIG_INVALID",
        message,
        detail=detail,
    )


def model_transport_error(message: str, *, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.MODEL_TRANSPORT_FAILURE,
        "MODEL_TRANSPORT_ERROR",
        message,
        retryable=True,
        detail=detail,
    )


def model_timeout(*, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.WORKER_TIMEOUT,
        "MODEL_TIMEOUT",
        "模型服务请求超时。",
        retryable=True,
        detail=detail,
    )


def model_rate_limited(*, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.MODEL_TRANSPORT_FAILURE,
        "MODEL_RATE_LIMITED",
        "模型服务触发限流。",
        retryable=True,
        detail=detail,
    )


def model_provider_unavailable(*, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.MODEL_TRANSPORT_FAILURE,
        "MODEL_PROVIDER_UNAVAILABLE",
        "模型服务暂不可用。",
        retryable=True,
        detail=detail,
    )


def model_request_rejected(*, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.MODEL_SEMANTIC_FAILURE,
        "MODEL_REQUEST_REJECTED",
        "模型服务拒绝本次请求。",
        detail=detail,
    )


def model_output_empty(*, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.OUTPUT_FORMAT_FAILURE,
        "MODEL_OUTPUT_EMPTY",
        "模型服务未返回可用内容。",
        detail=detail,
    )


def model_output_invalid_json(*, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.OUTPUT_FORMAT_FAILURE,
        "MODEL_OUTPUT_INVALID_JSON",
        "模型输出不是合法 JSON 结构。",
        detail=detail,
    )


def model_stream_chunk_invalid(*, detail: dict[str, object] | None = None) -> WorkerError:
    return WorkerError(
        WorkerErrorType.OUTPUT_FORMAT_FAILURE,
        "MODEL_STREAM_CHUNK_INVALID",
        "模型流式响应 chunk 不合法。",
        detail=detail,
    )
