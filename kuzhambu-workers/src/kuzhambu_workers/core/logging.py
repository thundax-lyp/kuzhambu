from collections.abc import Mapping, Sequence
from typing import Any

REDACTED = "[REDACTED]"
SENSITIVE_KEYS = {
    "api_key",
    "apikey",
    "authorization",
    "content",
    "input",
    "payload",
    "prompt",
    "secret",
    "signature",
    "token",
}


def redact_data(value: Any) -> Any:
    if isinstance(value, Mapping):
        return {key: _redact_mapping_value(str(key), item) for key, item in value.items()}
    if isinstance(value, Sequence) and not isinstance(value, str | bytes | bytearray):
        return [redact_data(item) for item in value]
    return value


def _redact_mapping_value(key: str, value: Any) -> Any:
    normalized = key.replace("-", "_").replace(" ", "_").lower()
    if (
        normalized in SENSITIVE_KEYS
        or normalized.endswith("_key")
        or normalized.endswith("_token")
        or any(fragment in normalized for fragment in SENSITIVE_KEYS)
    ):
        return REDACTED
    return redact_data(value)
