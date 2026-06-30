from base64 import b64encode
from dataclasses import asdict, dataclass
from datetime import datetime, timedelta, timezone
from hashlib import sha256
from json import dumps, loads
from math import ceil
from pathlib import Path
from uuid import uuid4


@dataclass(frozen=True)
class ArtifactMetadata:
    artifact_id: str
    request_id: str
    format: str
    filename: str
    content_type: str
    size_bytes: int
    sha256: str
    chunk_count: int
    download_path: str
    created_at: str
    expires_at: str


@dataclass(frozen=True)
class ArtifactChunk:
    artifact_id: str
    format: str
    filename: str
    content_type: str
    encoding: str
    chunk_index: int
    chunk_count: int
    chunk: str
    chunk_sha256: str
    total_size_bytes: int
    sha256: str


class RequestArtifactStore:
    def __init__(self, request_id: str, root_dir: Path, chunk_bytes: int, ttl_hours: int = 12) -> None:
        if chunk_bytes <= 0:
            raise ValueError("chunk_bytes must be positive")
        if ttl_hours <= 0:
            raise ValueError("ttl_hours must be positive")
        self.request_id = request_id
        self.root_dir = root_dir
        self.chunk_bytes = chunk_bytes
        self.ttl_hours = ttl_hours
        self.artifact_dir = root_dir / "artifacts"
        self.artifact_dir.mkdir(parents=True, exist_ok=True)
        self._artifacts: dict[str, ArtifactMetadata] = {}

    def put_bytes(
        self,
        *,
        data: bytes,
        format: str,
        filename: str,
        content_type: str,
    ) -> ArtifactMetadata:
        artifact_id = f"art_{uuid4().hex}"
        artifact_path = self._artifact_path(artifact_id)
        metadata_path = self._metadata_path(artifact_id)
        artifact_path.write_bytes(data)
        created_at = _now()
        expires_at = _expires_at(self.ttl_hours)
        metadata = ArtifactMetadata(
            artifact_id=artifact_id,
            request_id=self.request_id,
            format=format,
            filename=filename,
            content_type=content_type,
            size_bytes=len(data),
            sha256=_digest(data),
            chunk_count=max(1, ceil(len(data) / self.chunk_bytes)),
            download_path=f"/internal/artifacts/{artifact_id}",
            created_at=created_at,
            expires_at=expires_at,
        )
        metadata_path.write_text(dumps(_metadata_to_json(metadata)), encoding="utf-8")
        self._artifacts[artifact_id] = metadata
        return metadata

    def chunks(self, artifact_id: str) -> list[ArtifactChunk]:
        metadata = self.get_metadata(artifact_id)
        data = self._artifact_path(artifact_id).read_bytes()
        if not data:
            raw_chunks = [b""]
        else:
            raw_chunks = [
                data[index : index + self.chunk_bytes]
                for index in range(0, len(data), self.chunk_bytes)
            ]
        return [
            ArtifactChunk(
                artifact_id=metadata.artifact_id,
                format=metadata.format,
                filename=metadata.filename,
                content_type=metadata.content_type,
                encoding="BASE64_CHUNK",
                chunk_index=index,
                chunk_count=metadata.chunk_count,
                chunk=b64encode(chunk).decode(),
                chunk_sha256=_digest(chunk),
                total_size_bytes=metadata.size_bytes,
                sha256=metadata.sha256,
            )
            for index, chunk in enumerate(raw_chunks)
        ]

    def get_metadata(self, artifact_id: str) -> ArtifactMetadata:
        if artifact_id in self._artifacts:
            return self._artifacts[artifact_id]
        metadata_path = self._metadata_path(artifact_id)
        metadata = ArtifactMetadata(**loads(metadata_path.read_text(encoding="utf-8")))
        self._artifacts[artifact_id] = metadata
        return metadata

    def read_bytes(self, artifact_id: str) -> bytes:
        return self._artifact_path(artifact_id).read_bytes()

    def cleanup(self) -> None:
        self._artifacts.clear()

    def _artifact_path(self, artifact_id: str) -> Path:
        return self.artifact_dir / f"{artifact_id}.bin"

    def _metadata_path(self, artifact_id: str) -> Path:
        return self.artifact_dir / f"{artifact_id}.json"


def cleanup_expired_artifacts(root_dir: Path, now: datetime | None = None) -> int:
    artifact_dir = root_dir / "artifacts"
    if not artifact_dir.exists():
        return 0
    current = now or datetime.now(timezone.utc)
    deleted = 0
    for metadata_path in artifact_dir.glob("*.json"):
        metadata = ArtifactMetadata(**loads(metadata_path.read_text(encoding="utf-8")))
        expires_at = datetime.fromisoformat(metadata.expires_at.replace("Z", "+00:00"))
        if current < expires_at:
            continue
        artifact_path = artifact_dir / f"{metadata.artifact_id}.bin"
        if artifact_path.exists():
            artifact_path.unlink()
        metadata_path.unlink()
        deleted += 1
    return deleted


def _digest(data: bytes) -> str:
    return f"sha256:{sha256(data).hexdigest()}"


def _now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="milliseconds").replace("+00:00", "Z")


def _expires_at(ttl_hours: int) -> str:
    return (datetime.now(timezone.utc) + timedelta(hours=ttl_hours)).isoformat(
        timespec="milliseconds"
    ).replace("+00:00", "Z")


def _metadata_to_json(metadata: ArtifactMetadata) -> dict[str, str | int]:
    return {
        key: value
        for key, value in asdict(metadata).items()
    }
