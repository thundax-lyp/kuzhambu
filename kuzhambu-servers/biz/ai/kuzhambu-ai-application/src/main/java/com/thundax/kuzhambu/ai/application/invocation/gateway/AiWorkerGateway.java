package com.thundax.kuzhambu.ai.application.invocation.gateway;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import java.time.Instant;
import java.util.function.Consumer;

public interface AiWorkerGateway {

    AiInvokeResult invoke(AiInvokeCommand command);

    void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer);

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
