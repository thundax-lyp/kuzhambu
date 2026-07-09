from time import monotonic
from typing import Any

from kuzhambu_workers.schemas.common import UsageSummary


def monotonic_ms() -> int:
    return int(monotonic() * 1000)


def elapsed_ms(start_ms: int) -> int:
    return max(monotonic_ms() - start_ms, 0)


def usage_from_provider(provider_usage: Any, *, latency_ms: int = 0) -> UsageSummary:
    if not isinstance(provider_usage, dict):
        return UsageSummary(latencyMs=latency_ms)
    return UsageSummary(
        latencyMs=latency_ms,
        inputTokens=_int_value(provider_usage.get("prompt_tokens")),
        outputTokens=_int_value(provider_usage.get("completion_tokens")),
        costAmount="0.00",
    )


def _int_value(value: Any) -> int:
    if isinstance(value, int) and value >= 0:
        return value
    return 0
