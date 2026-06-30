from datetime import datetime, timedelta, timezone
from json import dumps

from kuzhambu_workers.render.artifact_store import cleanup_expired_artifacts


def test_cleanup_expired_artifacts_deletes_expired_files(tmp_path) -> None:
    artifact_dir = tmp_path / "artifacts"
    artifact_dir.mkdir(parents=True, exist_ok=True)
    artifact_id = "art_expired"
    (artifact_dir / f"{artifact_id}.bin").write_bytes(b"payload")
    (artifact_dir / f"{artifact_id}.json").write_text(
        dumps(
            {
                "artifact_id": artifact_id,
                "request_id": "req-1",
                "format": "MP4",
                "filename": "video.mp4",
                "content_type": "video/mp4",
                "size_bytes": 7,
                "sha256": "sha256:test",
                "chunk_count": 1,
                "download_path": f"/internal/artifacts/{artifact_id}",
                "created_at": "2026-06-30T00:00:00.000Z",
                "expires_at": "2026-06-30T00:00:01.000Z",
            }
        ),
        encoding="utf-8",
    )

    deleted = cleanup_expired_artifacts(
        tmp_path, now=datetime(2026, 6, 30, 0, 0, 2, tzinfo=timezone.utc)
    )

    assert deleted == 1
    assert not (artifact_dir / f"{artifact_id}.bin").exists()
    assert not (artifact_dir / f"{artifact_id}.json").exists()


def test_cleanup_expired_artifacts_keeps_unexpired_files(tmp_path) -> None:
    artifact_dir = tmp_path / "artifacts"
    artifact_dir.mkdir(parents=True, exist_ok=True)
    artifact_id = "art_active"
    expires_at = datetime.now(timezone.utc) + timedelta(hours=1)
    (artifact_dir / f"{artifact_id}.bin").write_bytes(b"payload")
    (artifact_dir / f"{artifact_id}.json").write_text(
        dumps(
            {
                "artifact_id": artifact_id,
                "request_id": "req-1",
                "format": "PNG",
                "filename": "image.png",
                "content_type": "image/png",
                "size_bytes": 7,
                "sha256": "sha256:test",
                "chunk_count": 1,
                "download_path": f"/internal/artifacts/{artifact_id}",
                "created_at": "2026-06-30T00:00:00.000Z",
                "expires_at": expires_at.isoformat(timespec="milliseconds").replace("+00:00", "Z"),
            }
        ),
        encoding="utf-8",
    )

    deleted = cleanup_expired_artifacts(tmp_path, now=datetime.now(timezone.utc))

    assert deleted == 0
    assert (artifact_dir / f"{artifact_id}.bin").exists()
    assert (artifact_dir / f"{artifact_id}.json").exists()
