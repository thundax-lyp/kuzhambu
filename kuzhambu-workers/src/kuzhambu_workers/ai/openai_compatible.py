from collections.abc import Iterator
from dataclasses import dataclass
from typing import Any

import httpx

from kuzhambu_workers.ai.errors import (
    model_output_empty,
    model_provider_unavailable,
    model_rate_limited,
    model_request_rejected,
    model_timeout,
    model_transport_error,
)
from kuzhambu_workers.ai.model_adapters import prepare_openai_compatible_invocation
from kuzhambu_workers.ai.prompt_messages import build_openai_messages
from kuzhambu_workers.ai.usage import elapsed_ms, monotonic_ms, usage_from_provider
from kuzhambu_workers.schemas.ai import AiInvokeRequest
from kuzhambu_workers.schemas.common import UsageSummary


@dataclass(frozen=True)
class OpenAiChatCompletionRequest:
    model: str
    messages: list[dict[str, str]]
    stream: bool
    response_format: dict[str, str] | None
    parameters: dict[str, Any]


@dataclass(frozen=True)
class OpenAiChatCompletionResult:
    content: str
    usage: UsageSummary
    raw_finish_reason: str | None


@dataclass(frozen=True)
class OpenAiChatCompletionChunk:
    delta: str
    usage: UsageSummary | None
    finish_reason: str | None


def invoke_chat_completion(
    request: AiInvokeRequest,
    *,
    client: httpx.Client | None = None,
    response_format: dict[str, str] | None = None,
) -> OpenAiChatCompletionResult:
    invocation = prepare_openai_compatible_invocation(request.modelConfig)
    chat_request = build_chat_completion_request(
        request,
        stream=False,
        response_format=response_format,
    )
    body = _request_body(chat_request)
    headers = _request_headers(request.modelConfig.apiKey)
    start_ms = monotonic_ms()
    try:
        response = _post_json(
            invocation.chat_completions_url,
            body=body,
            headers=headers,
            timeout_seconds=invocation.timeout_ms / 1000,
            client=client,
        )
    except httpx.TimeoutException as exc:
        raise model_timeout(detail={"errorClass": type(exc).__name__}) from exc
    except httpx.RequestError as exc:
        raise model_transport_error(
            "模型服务网络请求失败。",
            detail={"errorClass": type(exc).__name__},
        ) from exc

    latency_ms = elapsed_ms(start_ms)
    _raise_for_provider_status(response)
    payload = _json_payload(response)
    content = _message_content(payload)
    finish_reason = _finish_reason(payload)
    usage = usage_from_provider(payload.get("usage"), latency_ms=latency_ms)
    return OpenAiChatCompletionResult(
        content=content,
        usage=usage,
        raw_finish_reason=finish_reason,
    )


def build_chat_completion_request(
    request: AiInvokeRequest,
    *,
    stream: bool,
    response_format: dict[str, str] | None = None,
) -> OpenAiChatCompletionRequest:
    invocation = prepare_openai_compatible_invocation(request.modelConfig)
    return OpenAiChatCompletionRequest(
        model=invocation.model_name,
        messages=build_openai_messages(request.prompt),
        stream=stream,
        response_format=response_format,
        parameters=invocation.parameters,
    )


def _request_body(request: OpenAiChatCompletionRequest) -> dict[str, Any]:
    body: dict[str, Any] = {
        "model": request.model,
        "messages": request.messages,
        "stream": request.stream,
    }
    if request.response_format is not None:
        body["response_format"] = request.response_format
    body.update(request.parameters)
    return body


def _request_headers(api_key: str) -> dict[str, str]:
    return {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
    }


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


def _raise_for_provider_status(response: httpx.Response) -> None:
    if response.status_code < 400:
        return

    detail = _provider_error_detail(response)
    if response.status_code == 429:
        raise model_rate_limited(detail=detail)
    if response.status_code >= 500:
        raise model_provider_unavailable(detail=detail)
    raise model_request_rejected(detail=detail)


def _provider_error_detail(response: httpx.Response) -> dict[str, object]:
    detail: dict[str, object] = {"statusCode": response.status_code}
    provider_error_type = _provider_error_type(response)
    if provider_error_type:
        detail["providerErrorType"] = provider_error_type
    return detail


def _provider_error_type(response: httpx.Response) -> str | None:
    try:
        payload = response.json()
    except ValueError:
        return None
    if not isinstance(payload, dict):
        return None
    error = payload.get("error")
    if not isinstance(error, dict):
        return None
    error_type = error.get("type")
    if isinstance(error_type, str) and error_type.strip():
        return error_type
    return None


def _json_payload(response: httpx.Response) -> dict[str, Any]:
    try:
        payload = response.json()
    except ValueError as exc:
        raise model_output_empty(detail={"statusCode": response.status_code}) from exc
    if not isinstance(payload, dict):
        raise model_output_empty(detail={"statusCode": response.status_code})
    return payload


def _message_content(payload: dict[str, Any]) -> str:
    choice = _first_choice(payload)
    message = choice.get("message")
    if not isinstance(message, dict):
        raise model_output_empty()
    content = message.get("content")
    if not isinstance(content, str) or not content.strip():
        raise model_output_empty()
    return content


def _finish_reason(payload: dict[str, Any]) -> str | None:
    choice = _first_choice(payload)
    finish_reason = choice.get("finish_reason")
    if isinstance(finish_reason, str):
        return finish_reason
    return None


def _first_choice(payload: dict[str, Any]) -> dict[str, Any]:
    choices = payload.get("choices")
    if not isinstance(choices, list) or not choices:
        raise model_output_empty()
    first = choices[0]
    if not isinstance(first, dict):
        raise model_output_empty()
    return first


def iter_chat_completion_chunks() -> Iterator[OpenAiChatCompletionChunk]:
    raise NotImplementedError("streaming is implemented by the SSE task")
