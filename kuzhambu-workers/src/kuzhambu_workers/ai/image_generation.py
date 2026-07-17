from base64 import b64decode
from binascii import Error as Base64Error
from dataclasses import dataclass
from typing import Any

import httpx

from kuzhambu_workers.ai.errors import (
    model_provider_unavailable,
    model_rate_limited,
    model_request_rejected,
    model_timeout,
    model_transport_error,
)
from kuzhambu_workers.ai.model_adapters import prepare_openai_compatible_invocation
from kuzhambu_workers.ai.usage import elapsed_ms, monotonic_ms
from kuzhambu_workers.core.errors import WorkerError, WorkerErrorType
from kuzhambu_workers.schemas.ai import AiInvokeRequest, AiMessage
from kuzhambu_workers.schemas.common import UsageSummary

IMAGE_GENERATIONS_PATH = "/" + "/".join(("images", "generations"))
IMAGE_GENERATION_RESERVED_PROVIDER_PARAMETERS = frozenset({"model", "messages"})
IMAGE_GENERATION_OPTIONAL_PROVIDER_PARAMETERS = (
    "response_format",
    "n",
    "size",
    "quality",
    "style",
    "output_format",
    "background",
    "stream",
    "watermark",
)
SUPPORTED_IMAGE_CONTENT_TYPES = {
    "image/png": "sancai-image.png",
    "image/jpeg": "sancai-image.jpg",
    "image/webp": "sancai-image.webp",
}
OUTPUT_FORMAT_CONTENT_TYPES = {
    "png": "image/png",
    "jpeg": "image/jpeg",
    "jpg": "image/jpeg",
    "webp": "image/webp",
}


@dataclass(frozen=True)
class GeneratedImageArtifact:
    data: bytes
    content_type: str
    filename: str
    usage: UsageSummary


def generate_image(
    request: AiInvokeRequest,
    *,
    client: httpx.Client | None = None,
) -> GeneratedImageArtifact:
    invocation = prepare_openai_compatible_invocation(
        request.modelConfig,
        reserved_provider_parameters=IMAGE_GENERATION_RESERVED_PROVIDER_PARAMETERS,
    )
    body = _request_body(request, invocation.model_name, invocation.parameters)
    headers = _request_headers(request.modelConfig.apiKey)
    start_ms = monotonic_ms()
    try:
        response = _post_json(
            f"{invocation.base_url}{IMAGE_GENERATIONS_PATH}",
            body=body,
            headers=headers,
            timeout_seconds=invocation.timeout_ms / 1000,
            client=client,
        )
    except httpx.TimeoutException as exc:
        raise model_timeout(detail={"errorClass": type(exc).__name__}) from exc
    except httpx.RequestError as exc:
        raise model_transport_error(
            "图片生成模型服务网络请求失败。",
            detail={"errorClass": type(exc).__name__},
        ) from exc

    latency_ms = elapsed_ms(start_ms)
    _raise_for_provider_status(response)
    payload = _json_payload(response)
    return _decode_image_response(payload, latency_ms=latency_ms)


def _request_body(
    request: AiInvokeRequest,
    model_name: str,
    parameters: dict[str, Any],
) -> dict[str, Any]:
    body: dict[str, Any] = {
        "model": model_name,
        "prompt": _build_image_prompt(request.prompt.messages),
        "response_format": "b64_json",
        "n": 1,
    }
    for field_name in IMAGE_GENERATION_OPTIONAL_PROVIDER_PARAMETERS:
        if field_name in parameters:
            body[field_name] = parameters[field_name]
    return body


def _build_image_prompt(messages: list[AiMessage]) -> str:
    parts = [
        f"{message.role.value}: {message.content.strip()}"
        for message in messages
        if isinstance(message.content, str) and message.content.strip()
    ]
    if not parts:
        raise _model_semantic_failure("IMAGE_PROMPT_EMPTY", "图片生成 prompt 不能为空。")
    return "\n".join(parts)


def _decode_image_response(
    payload: dict[str, Any],
    *,
    latency_ms: int,
) -> GeneratedImageArtifact:
    image_payload = _first_image_payload(payload)
    content_type = _content_type(payload, image_payload)
    b64_json = image_payload.get("b64_json")
    if isinstance(b64_json, str) and b64_json.strip():
        data = _decode_base64_image(b64_json)
    else:
        url = image_payload.get("url")
        if not isinstance(url, str) or not url.strip():
            raise _model_semantic_failure(
                "IMAGE_OUTPUT_EMPTY",
                "图片生成模型未返回可用图片。",
            )
        data = _download_image(url, expected_content_type=content_type)
    _validate_image_bytes(data, content_type)
    return GeneratedImageArtifact(
        data=data,
        content_type=content_type,
        filename=SUPPORTED_IMAGE_CONTENT_TYPES[content_type],
        usage=_usage_from_image_provider(payload.get("usage"), latency_ms=latency_ms),
    )


def _first_image_payload(payload: dict[str, Any]) -> dict[str, Any]:
    data = payload.get("data")
    if not isinstance(data, list) or not data:
        raise _model_semantic_failure("IMAGE_OUTPUT_EMPTY", "图片生成模型未返回图片。")
    image_payload = data[0]
    if not isinstance(image_payload, dict):
        raise _output_format_failure("IMAGE_OUTPUT_INVALID", "图片生成结果结构不合法。")
    return image_payload


def _content_type(payload: dict[str, Any], image_payload: dict[str, Any]) -> str:
    raw_output_format = image_payload.get("output_format") or payload.get("output_format")
    if isinstance(raw_output_format, str) and raw_output_format.strip():
        content_type = OUTPUT_FORMAT_CONTENT_TYPES.get(raw_output_format.strip().lower())
        if content_type is None:
            raise _image_input_failure(
                "IMAGE_OUTPUT_FORMAT_UNSUPPORTED",
                "图片生成模型返回了不支持的图片格式。",
                detail={"outputFormat": raw_output_format},
            )
        return content_type
    return "image/png"


def _decode_base64_image(value: str) -> bytes:
    try:
        return b64decode(value, validate=True)
    except (Base64Error, ValueError) as exc:
        raise _output_format_failure("IMAGE_BASE64_INVALID", "图片 base64 内容不合法。") from exc


def _download_image(url: str, *, expected_content_type: str) -> bytes:
    try:
        with httpx.Client(timeout=30) as client:
            response = client.get(url)
    except httpx.TimeoutException as exc:
        raise model_timeout(detail={"errorClass": type(exc).__name__}) from exc
    except httpx.RequestError as exc:
        raise model_transport_error(
            "模型临时图片下载失败。",
            detail={"errorClass": type(exc).__name__},
        ) from exc
    if response.status_code >= 400:
        raise model_provider_unavailable(detail={"statusCode": response.status_code})
    response_content_type = response.headers.get("content-type", "").split(";")[0].strip()
    if response_content_type and response_content_type != expected_content_type:
        raise _image_input_failure(
            "IMAGE_CONTENT_TYPE_MISMATCH",
            "模型临时图片 Content-Type 不符合预期。",
            detail={
                "expectedContentType": expected_content_type,
                "actualContentType": response_content_type,
            },
        )
    return response.content


def _validate_image_bytes(data: bytes, content_type: str) -> None:
    if not data:
        raise _model_semantic_failure("IMAGE_OUTPUT_EMPTY", "图片生成模型返回空图片。")
    if content_type == "image/png" and not data.startswith(b"\x89PNG\r\n\x1a\n"):
        raise _image_input_failure("IMAGE_BYTES_INVALID", "PNG 图片内容不合法。")
    if content_type == "image/jpeg" and not data.startswith(b"\xff\xd8"):
        raise _image_input_failure("IMAGE_BYTES_INVALID", "JPEG 图片内容不合法。")
    if content_type == "image/webp" and not (data.startswith(b"RIFF") and data[8:12] == b"WEBP"):
        raise _image_input_failure("IMAGE_BYTES_INVALID", "WEBP 图片内容不合法。")


def _usage_from_image_provider(provider_usage: Any, *, latency_ms: int) -> UsageSummary:
    if not isinstance(provider_usage, dict):
        return UsageSummary(latencyMs=latency_ms)
    return UsageSummary(
        latencyMs=latency_ms,
        inputTokens=_int_value(provider_usage.get("input_tokens")),
        outputTokens=_int_value(provider_usage.get("output_tokens")),
        costAmount="0.00",
    )


def _int_value(value: Any) -> int:
    if isinstance(value, int) and value >= 0:
        return value
    return 0


def _post_json(
    url: str,
    *,
    body: dict[str, Any],
    headers: dict[str, str],
    timeout_seconds: float,
    client: httpx.Client | None,
) -> httpx.Response:
    if client is not None:
        return client.post(url, json=body, headers=headers, timeout=timeout_seconds)
    with httpx.Client(timeout=timeout_seconds) as owned_client:
        return owned_client.post(url, json=body, headers=headers)


def _request_headers(api_key: str) -> dict[str, str]:
    return {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
    }


def _raise_for_provider_status(response: httpx.Response) -> None:
    if response.status_code < 400:
        return
    detail = {"statusCode": response.status_code}
    if response.status_code == 429:
        raise model_rate_limited(detail=detail)
    if response.status_code >= 500:
        raise model_provider_unavailable(detail=detail)
    raise model_request_rejected(detail=detail)


def _json_payload(response: httpx.Response) -> dict[str, Any]:
    try:
        payload = response.json()
    except ValueError as exc:
        raise _output_format_failure(
            "IMAGE_RESPONSE_INVALID_JSON", "图片生成响应不是合法 JSON。"
        ) from exc
    if not isinstance(payload, dict):
        raise _output_format_failure("IMAGE_RESPONSE_INVALID", "图片生成响应结构不合法。")
    return payload


def _model_semantic_failure(
    code: str,
    message: str,
    *,
    detail: dict[str, Any] | None = None,
) -> WorkerError:
    return WorkerError(
        WorkerErrorType.MODEL_SEMANTIC_FAILURE,
        code,
        message,
        detail=detail,
    )


def _output_format_failure(code: str, message: str) -> WorkerError:
    return WorkerError(
        WorkerErrorType.OUTPUT_FORMAT_FAILURE,
        code,
        message,
    )


def _image_input_failure(
    code: str,
    message: str,
    *,
    detail: dict[str, Any] | None = None,
) -> WorkerError:
    return WorkerError(
        WorkerErrorType.IMAGE_INPUT_FAILURE,
        code,
        message,
        detail=detail,
    )
