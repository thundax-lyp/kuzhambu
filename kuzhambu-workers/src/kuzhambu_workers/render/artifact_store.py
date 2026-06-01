from base64 import b64encode
from dataclasses import dataclass
from hashlib import sha256
from math import ceil
from pathlib import Path
from uuid import uuid4


@dataclass(frozen=True)
class ArtifactMetadata:
    artifact_id: str
    format: str
    filename: str
    content_type: str
    size_bytes: int
    sha256: str
    chunk_count: int


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
    def __init__(self, request_id: str, root_dir: Path, chunk_bytes: int) -> None:
        if chunk_bytes <= 0:
            raise ValueError("chunk_bytes must be positive")
        self.request_id = request_id
        self.root_dir = root_dir
        self.chunk_bytes = chunk_bytes
        self.request_dir = root_dir / request_id
        self.request_dir.mkdir(parents=True, exist_ok=True)
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
        artifact_path = self.request_dir / artifact_id
        artifact_path.write_bytes(data)
        metadata = ArtifactMetadata(
            artifact_id=artifact_id,
            format=format,
            filename=filename,
            content_type=content_type,
            size_bytes=len(data),
            sha256=_digest(data),
            chunk_count=max(1, ceil(len(data) / self.chunk_bytes)),
        )
        self._artifacts[artifact_id] = metadata
        return metadata

    def chunks(self, artifact_id: str) -> list[ArtifactChunk]:
        metadata = self._artifacts[artifact_id]
        data = (self.request_dir / artifact_id).read_bytes()
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

    def cleanup(self) -> None:
        if not self.request_dir.exists():
            return
        for path in sorted(self.request_dir.rglob("*"), reverse=True):
            if path.is_file():
                path.unlink()
            elif path.is_dir():
                path.rmdir()
        self.request_dir.rmdir()
        self._artifacts.clear()


def _digest(data: bytes) -> str:
    return f"sha256:{sha256(data).hexdigest()}"
