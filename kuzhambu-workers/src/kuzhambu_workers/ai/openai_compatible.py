import json
from collections.abc import Iterator
from dataclasses import dataclass
from typing import Any

import httpx

from kuzhambu_workers.ai.errors import (
    model_output_empty,
    model_provider_unavailable,
    model_rate_limited,
    model_request_rejected,
    model_stream_chunk_invalid,
    model_timeout,
    model_transport_error,
)
from kuzhambu_workers.ai.model_adapters import prepare_openai_compatible_invocation
from kuzhambu_workers.ai.prompt_messages import build_openai_messages
from kuzhambu_workers.ai.structured_output import openai_response_format
from kuzhambu_workers.ai.usage import elapsed_ms, monotonic_ms, usage_from_provider
from kuzhambu_workers.schemas.ai import AiInvokeRequest
from kuzhambu_workers.schemas.common import UsageSummary


@dataclass(frozen=True)
class OpenAiChatCompletionRequest:
    model: str
    messages: list[dict[str, Any]]
    stream: bool
    response_format: dict[str, Any] | None
    parameters: dict[str, Any]


@dataclass(frozen=True)
class OpenAiChatCompletionResult:
    content: str
    usage: UsageSummary
    raw_finish_reason: str | None
    provider_usage: bool = True


@dataclass(frozen=True)
class OpenAiChatCompletionChunk:
    delta: str
    usage: UsageSummary | None
    finish_reason: str | None
    provider_usage: bool = False


def invoke_chat_completion(
    request: AiInvokeRequest,
    *,
    client: httpx.Client | None = None,
    response_format: dict[str, Any] | None = None,
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
    provider_usage = payload.get("usage") is not None
    usage = usage_from_provider(payload.get("usage"), latency_ms=latency_ms)
    return OpenAiChatCompletionResult(
        content=content,
        usage=usage,
        raw_finish_reason=finish_reason,
        provider_usage=provider_usage,
    )


def build_chat_completion_request(
    request: AiInvokeRequest,
    *,
    stream: bool,
    response_format: dict[str, Any] | None = None,
) -> OpenAiChatCompletionRequest:
    invocation = prepare_openai_compatible_invocation(request.modelConfig)
    effective_response_format = response_format
    if effective_response_format is None:
        effective_response_format = openai_response_format(request)
    return OpenAiChatCompletionRequest(
        model=invocation.model_name,
        messages=build_openai_messages(request.prompt),
        stream=stream,
        response_format=effective_response_format,
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


def iter_chat_completion_chunks(
    request: AiInvokeRequest,
    *,
    client: httpx.Client | None = None,
    response_format: dict[str, Any] | None = None,
) -> Iterator[OpenAiChatCompletionChunk]:
    invocation = prepare_openai_compatible_invocation(request.modelConfig)
    chat_request = build_chat_completion_request(
        request,
        stream=True,
        response_format=response_format,
    )
    body = _request_body(chat_request)
    headers = _request_headers(request.modelConfig.apiKey)
    start_ms = monotonic_ms()
    try:
        if client is not None:
            with client.stream(
                "POST",
                invocation.chat_completions_url,
                json=body,
                headers=headers,
                timeout=invocation.timeout_ms / 1000,
            ) as response:
                yield from _iter_response_chunks(response, start_ms)
            return
        with httpx.Client(timeout=invocation.timeout_ms / 1000) as owned_client:
            with owned_client.stream(
                "POST",
                invocation.chat_completions_url,
                json=body,
                headers=headers,
            ) as response:
                yield from _iter_response_chunks(response, start_ms)
    except httpx.TimeoutException as exc:
        raise model_timeout(detail={"errorClass": type(exc).__name__}) from exc
    except httpx.RequestError as exc:
        raise model_transport_error(
            "模型服务流式请求失败。",
            detail={"errorClass": type(exc).__name__},
        ) from exc


def _iter_response_chunks(
    response: httpx.Response,
    start_ms: int,
) -> Iterator[OpenAiChatCompletionChunk]:
    _raise_for_provider_status(response)
    saw_usage = False
    for line in response.iter_lines():
        chunk = _parse_stream_line(line, start_ms)
        if chunk is None:
            continue
        saw_usage = saw_usage or chunk.usage is not None
        yield chunk
    if not saw_usage:
        yield OpenAiChatCompletionChunk(
            delta="",
            usage=UsageSummary(latencyMs=elapsed_ms(start_ms)),
            finish_reason=None,
            provider_usage=False,
        )


def _parse_stream_line(line: str, start_ms: int) -> OpenAiChatCompletionChunk | None:
    stripped = line.strip()
    if not stripped or not stripped.startswith("data:"):
        return None
    data = stripped.removeprefix("data:").strip()
    if data == "[DONE]":
        return None
    try:
        payload = json.loads(data)
    except json.JSONDecodeError as exc:
        raise model_stream_chunk_invalid() from exc
    except ValueError as exc:
        raise model_stream_chunk_invalid() from exc
    if not isinstance(payload, dict):
        raise model_stream_chunk_invalid()
    return _stream_chunk_from_payload(payload, start_ms)


def _stream_chunk_from_payload(
    payload: dict[str, Any],
    start_ms: int,
) -> OpenAiChatCompletionChunk:
    delta = ""
    finish_reason: str | None = None
    choices = payload.get("choices")
    if isinstance(choices, list) and choices:
        first = choices[0]
        if not isinstance(first, dict):
            raise model_stream_chunk_invalid()
        delta = _stream_delta(first)
        raw_finish_reason = first.get("finish_reason")
        if isinstance(raw_finish_reason, str):
            finish_reason = raw_finish_reason
    elif choices not in (None, []):
        raise model_stream_chunk_invalid()

    usage = None
    provider_usage = False
    if payload.get("usage") is not None:
        usage = usage_from_provider(payload.get("usage"), latency_ms=elapsed_ms(start_ms))
        provider_usage = True
    if not delta and finish_reason is None and usage is None:
        if _is_empty_choice_chunk(payload):
            return OpenAiChatCompletionChunk(delta="", usage=None, finish_reason=None)
        raise model_stream_chunk_invalid()
    return OpenAiChatCompletionChunk(
        delta=delta,
        usage=usage,
        finish_reason=finish_reason,
        provider_usage=provider_usage,
    )


def _is_empty_choice_chunk(payload: dict[str, Any]) -> bool:
    choices = payload.get("choices")
    if not isinstance(choices, list) or not choices:
        return False
    first = choices[0]
    if not isinstance(first, dict):
        return False
    delta = first.get("delta")
    if not isinstance(delta, dict):
        return False
    return True


def _stream_delta(choice: dict[str, Any]) -> str:
    delta = choice.get("delta")
    if delta is None:
        return ""
    if not isinstance(delta, dict):
        raise model_stream_chunk_invalid()
    content = delta.get("content")
    if content is None:
        return ""
    if not isinstance(content, str):
        raise model_stream_chunk_invalid()
    return content
