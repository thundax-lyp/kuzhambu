package com.thundax.kuzhambu.ai.infra.client;

import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import java.time.Instant;

public interface WorkerAiClient extends AiWorkerInvocationApplicationService.WorkerAiClient {

    DownloadedArtifact downloadArtifact(String requestId, String traceId, String downloadPath);

    record DownloadedArtifact(
            byte[] data, String contentType, String filename, String sha256, long sizeBytes, Instant expiresAt) {}

    class ArtifactDownloadException extends RuntimeException {

        public ArtifactDownloadException(String message) {
            super(message);
        }

        public ArtifactDownloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
