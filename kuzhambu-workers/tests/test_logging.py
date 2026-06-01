from kuzhambu_workers.core.logging import REDACTED, redact_data


def test_redact_data_masks_sensitive_values() -> None:
    payload = {
        "modelConfig": {
            "apiKey": "model-secret",
        },
        "headers": {
            "Authorization": "Bearer token",
            "X-Kuzhambu-Signature": "signature",
        },
        "prompt": {
            "messages": [{"role": "user", "content": "full prompt"}],
        },
        "input": {
            "payload": {"text": "full business payload"},
        },
        "traceId": "trace-1",
    }

    redacted = redact_data(payload)

    assert redacted["modelConfig"]["apiKey"] == REDACTED
    assert redacted["headers"]["Authorization"] == REDACTED
    assert redacted["headers"]["X-Kuzhambu-Signature"] == REDACTED
    assert redacted["prompt"] == REDACTED
    assert redacted["input"] == REDACTED
    assert redacted["traceId"] == "trace-1"
