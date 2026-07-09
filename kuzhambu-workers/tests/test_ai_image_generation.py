import json
from base64 import b64encode

import httpx

from kuzhambu_workers.ai.image_generation import generate_image
from kuzhambu_workers.schemas.ai import AiInvokeRequest

PNG_1X1 = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x06\x00\x00\x00\x1f\x15\xc4\x89\x00\x00\x00\rIDATx\x9cc\xf8"
    b"\xff\xff?\x00\x05\xfe\x02\xfeA\xe2i\xb3\x00\x00\x00\x00IEND\xaeB`\x82"
)


def test_generate_image_posts_openai_compatible_image_request() -> None:
    captured: dict[str, object] = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["url"] = str(request.url)
        captured["authorization"] = request.headers["Authorization"]
        captured["content_type"] = request.headers["Content-Type"]
        captured["body"] = json.loads(request.content)
        return httpx.Response(
            200,
            json={"data": [{"b64_json": b64encode(PNG_1X1).decode("ascii")}]},
        )

    client = httpx.Client(transport=httpx.MockTransport(handler))
    request = AiInvokeRequest.model_validate(_request_payload())

    artifact = generate_image(request, client=client)

    assert captured["url"] == "https://ark.example/v1/images/generations"
    assert captured["authorization"] == "Bearer process-only"
    assert captured["content_type"] == "application/json"
    assert captured["body"] == {
        "model": "image-model",
        "prompt": "system: image instruction\nuser: draw a cup",
        "response_format": "url",
        "n": 1,
        "size": "2K",
        "stream": False,
        "watermark": True,
    }
    assert artifact.data == PNG_1X1
    assert artifact.content_type == "image/png"


def _request_payload() -> dict:
    return {
        "requestId": "req-1",
        "traceId": "trace-1",
        "callerDomain": "AI",
        "operation": "CLASSICS_SANCAI_IMAGE_GEN",
        "capability": "image_gen",
        "scope": "SANCAI",
        "modelConfig": {
            "serviceRole": "TEXT2IMAGE",
            "apiSource": "OPENAI_COMPATIBLE",
            "baseUrl": "https://ark.example/v1",
            "apiKey": "process-only",
            "modelName": "image-model",
            "capabilityTags": ["image_gen"],
            "parameters": {
                "response_format": "url",
                "size": "2K",
                "stream": False,
                "watermark": True,
            },
            "timeoutMs": 60000,
        },
        "prompt": {
            "messages": [
                {"role": "system", "content": "image instruction"},
                {"role": "user", "content": "draw a cup"},
            ],
        },
        "input": {
            "contentType": "SANCAI_ENTRY",
            "payload": {},
        },
        "outputSchema": {"type": "artifact"},
    }
