from base64 import b64decode

from kuzhambu_workers.render.artifact_store import RequestArtifactStore
from kuzhambu_workers.streaming.events import artifact_chunk_event
from kuzhambu_workers.streaming.sse import encode_sse


def test_artifact_store_chunks_and_hashes(tmp_path) -> None:
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=4)
    metadata = store.put_bytes(
        data=b"hello world",
        format="PDF",
        filename="report.pdf",
        content_type="application/pdf",
    )

    chunks = store.chunks(metadata.artifact_id)

    assert metadata.size_bytes == 11
    assert metadata.chunk_count == 3
    assert [chunk.chunk_index for chunk in chunks] == [0, 1, 2]
    assert b"".join(b64decode(chunk.chunk) for chunk in chunks) == b"hello world"
    assert all(chunk.sha256 == metadata.sha256 for chunk in chunks)
    assert all(chunk.chunk_sha256.startswith("sha256:") for chunk in chunks)


def test_artifact_store_cleans_request_directory(tmp_path) -> None:
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=4)
    metadata = store.put_bytes(
        data=b"payload",
        format="ZIP",
        filename="export.zip",
        content_type="application/zip",
    )
    assert (tmp_path / "req-1" / metadata.artifact_id).exists()

    store.cleanup()

    assert not (tmp_path / "req-1").exists()


def test_artifact_chunk_event_matches_sse_contract(tmp_path) -> None:
    store = RequestArtifactStore("req-1", tmp_path, chunk_bytes=4)
    metadata = store.put_bytes(
        data=b"hello",
        format="HTML",
        filename="page.html",
        content_type="text/html; charset=utf-8",
    )
    chunk = store.chunks(metadata.artifact_id)[0]

    encoded = encode_sse(
        artifact_chunk_event(
            request_id="req-1",
            trace_id="trace-1",
            timestamp="2026-06-01T10:00:00.000Z",
            chunk=chunk,
        )
    )

    assert "event: artifact" in encoded
    assert '"artifactId":"' in encoded
    assert '"chunkIndex":0' in encoded
    assert '"chunkSha256":"sha256:' in encoded
