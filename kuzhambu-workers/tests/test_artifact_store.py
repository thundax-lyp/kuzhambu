from base64 import b64decode
from datetime import datetime, timedelta, timezone
from json import loads

from kuzhambu_workers.render.artifact_store import RequestArtifactStore


def test_artifact_store_persists_metadata_and_bytes(tmp_path) -> None:
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=4, ttl_hours=12)

    metadata = store.put_bytes(
        data=b"hello world",
        format="PNG",
        filename="image.png",
        content_type="image/png",
    )

    assert metadata.request_id == "req-1"
    assert metadata.download_path == f"/internal/artifacts/{metadata.artifact_id}"
    assert metadata.size_bytes == 11
    assert metadata.chunk_count == 3
    assert store.read_bytes(metadata.artifact_id) == b"hello world"
    persisted = loads(
        (tmp_path / "artifacts" / f"{metadata.artifact_id}.json").read_text(encoding="utf-8")
    )
    assert persisted["artifact_id"] == metadata.artifact_id
    assert persisted["download_path"] == metadata.download_path


def test_artifact_store_cleanup_only_clears_memory_cache(tmp_path) -> None:
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=4, ttl_hours=12)

    metadata = store.put_bytes(
        data=b"payload",
        format="ZIP",
        filename="export.zip",
        content_type="application/zip",
    )
    store.cleanup()

    assert store.get_metadata(metadata.artifact_id).artifact_id == metadata.artifact_id
    assert (tmp_path / "artifacts" / f"{metadata.artifact_id}.bin").exists()
    assert (tmp_path / "artifacts" / f"{metadata.artifact_id}.json").exists()


def test_artifact_store_sets_expiry_from_ttl(tmp_path) -> None:
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=4, ttl_hours=12)

    metadata = store.put_bytes(
        data=b"payload",
        format="MP4",
        filename="video.mp4",
        content_type="video/mp4",
    )

    created_at = datetime.fromisoformat(metadata.created_at.replace("Z", "+00:00"))
    expires_at = datetime.fromisoformat(metadata.expires_at.replace("Z", "+00:00"))
    delta = expires_at - created_at
    assert delta >= timedelta(hours=11, minutes=59)
    assert delta <= timedelta(hours=12, minutes=1)
    assert expires_at.tzinfo == timezone.utc


def test_artifact_store_chunks_keep_image_gen_metadata(tmp_path) -> None:
    store = RequestArtifactStore("req-image-gen", tmp_path, chunk_bytes=5, ttl_hours=12)

    metadata = store.put_bytes(
        data=b"png-binary",
        format="PNG",
        filename="generated.png",
        content_type="image/png",
    )

    chunks = store.chunks(metadata.artifact_id)

    assert chunks[0].artifact_id == metadata.artifact_id
    assert chunks[0].format == "PNG"
    assert chunks[0].filename == "generated.png"
    assert chunks[0].content_type == "image/png"
    assert chunks[0].encoding == "BASE64_CHUNK"
    assert chunks[-1].chunk_index == metadata.chunk_count - 1
    assert b"".join(b64decode(chunk.chunk) for chunk in chunks) == b"png-binary"
