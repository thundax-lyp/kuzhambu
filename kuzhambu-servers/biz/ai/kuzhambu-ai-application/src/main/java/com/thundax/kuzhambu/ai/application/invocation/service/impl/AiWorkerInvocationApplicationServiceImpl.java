package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.ai.infra.client.WorkerAiClient;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.CompleteMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.InitMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadMultipartPartFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.CompleteMultipartUploadFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.InitMultipartUploadFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class AiWorkerInvocationApplicationServiceImpl implements AiWorkerInvocationApplicationService {

    private static final long MULTIPART_THRESHOLD_BYTES = 8L * 1024 * 1024;
    private static final long MULTIPART_PART_SIZE_BYTES = 5L * 1024 * 1024;

    private final AiInvocationRepository aiInvocationRepository;
    private final WorkerAiClient workerAiClient;
    private final StorageFacade storageFacade;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public AiWorkerInvocationApplicationServiceImpl(
            AiInvocationRepository aiInvocationRepository, WorkerAiClient workerAiClient, StorageFacade storageFacade) {
        this.aiInvocationRepository = aiInvocationRepository;
        this.workerAiClient = workerAiClient;
        this.storageFacade = storageFacade;
    }

    @Override
    public AiInvokeResult invoke(AiInvokeCommand command) {
        validateCommand(command);
        command.setStream(false);
        AiCallRecord callRecord = command.toRunningCallRecord();
        Long callId = aiInvocationRepository.saveCallRecord(callRecord);
        callRecord.setCallId(callId);
        AiInvokeResult result;
        try {
            result = workerAiClient.invoke(command);
        } catch (RuntimeException ex) {
            result = AiInvokeResult.failed(
                    command.getRequestId(),
                    command.getTraceId(),
                    "WORKER_UNAVAILABLE",
                    ex.getMessage(),
                    "WORKER_REQUEST");
        }
        return completeCall(command, callRecord, normalizeResult(command, result));
    }

    @Override
    public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        validateCommand(command);
        command.setStream(true);
        AiCallRecord callRecord = command.toRunningCallRecord();
        Long callId = aiInvocationRepository.saveCallRecord(callRecord);
        callRecord.setCallId(callId);
        AtomicReference<AiInvokeResult> completedResult = new AtomicReference<>();
        try {
            workerAiClient.stream(command, event -> handleStreamEvent(eventConsumer, completedResult, event));
        } catch (RuntimeException ex) {
            completedResult.compareAndSet(
                    null,
                    AiInvokeResult.failed(
                            command.getRequestId(),
                            command.getTraceId(),
                            "WORKER_UNAVAILABLE",
                            ex.getMessage(),
                            "WORKER_STREAM"));
        }
        AiInvokeResult result = completedResult.get();
        if (result == null) {
            result = AiInvokeResult.failed(
                    command.getRequestId(),
                    command.getTraceId(),
                    "WORKER_PROTOCOL_FAILURE",
                    "Worker stream ended without completed event",
                    "WORKER_STREAM");
        }
        return completeCall(command, callRecord, normalizeResult(command, result));
    }

    private void handleStreamEvent(
            Consumer<AiStreamEventResult> eventConsumer,
            AtomicReference<AiInvokeResult> completedResult,
            AiStreamEventResult event) {
        if (eventConsumer != null) {
            eventConsumer.accept(event);
        }
        if (event != null && event.isCompleted()) {
            completedResult.set(event.toInvokeResult());
        }
        if (event != null && event.isError()) {
            completedResult.compareAndSet(
                    null,
                    AiInvokeResult.failed(
                            event.getRequestId(),
                            event.getTraceId(),
                            event.getErrorType(),
                            event.getErrorMessage(),
                            event.getFailureStage()));
        }
    }

    private AiInvokeResult completeCall(AiInvokeCommand command, AiCallRecord callRecord, AiInvokeResult result) {
        Instant completedAt = Instant.now();
        if (result.isSucceeded()) {
            callRecord.markSucceeded(result.getUsage(), completedAt);
            callRecord.setWarningsJson(result.getWarningsJson());
            aiInvocationRepository.updateCallRecord(callRecord);
            if (command.isCreateCandidate()) {
                Long candidateId =
                        aiInvocationRepository.saveCandidate(result.toCandidate(command, callRecord.getCallId()));
                result.setCandidateId(candidateId);
            }
        } else {
            callRecord.markFailed(result.getErrorType(), result.getErrorMessage(), result.getUsage(), completedAt);
            aiInvocationRepository.updateCallRecord(callRecord);
        }
        result.setCallId(callRecord.getCallId());
        return result;
    }

    private AiInvokeResult normalizeResult(AiInvokeCommand command, AiInvokeResult result) {
        if (result == null) {
            return AiInvokeResult.failed(
                    command.getRequestId(),
                    command.getTraceId(),
                    "WORKER_PROTOCOL_FAILURE",
                    "Worker returned empty result");
        }
        if (result.getRequestId() == null) {
            result.setRequestId(command.getRequestId());
        }
        if (result.getTraceId() == null) {
            result.setTraceId(command.getTraceId());
        }
        if (result.getCapability() == null) {
            result.setCapability(command.getCapability());
        }
        if (result.isSucceeded() && !isBlank(result.getArtifactReferenceJson())) {
            return persistArtifactResult(command, result);
        }
        return result;
    }

    private void validateCommand(AiInvokeCommand command) {
        if (command == null
                || isBlank(command.getScope())
                || isBlank(command.getCapability())
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || command.getModelId() == null
                || isBlank(command.getModelName())
                || isBlank(command.getPromptMessagesJson())
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("AI invoke command is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private AiInvokeResult persistArtifactResult(AiInvokeCommand command, AiInvokeResult result) {
        try {
            JsonNode artifactReference = objectMapper.readTree(result.getArtifactReferenceJson());
            WorkerAiClient.DownloadedArtifact artifact = workerAiClient.downloadArtifact(
                    command.getRequestId(),
                    command.getTraceId(),
                    artifactReference.path("downloadPath").asText());
            result.setResultPayload(
                    artifact.sizeBytes() > MULTIPART_THRESHOLD_BYTES
                            ? toStorageResultJson(persistMultipartArtifact(artifact))
                            : toStorageResultJson(persistSmallArtifact(artifact)));
            return result;
        } catch (WorkerAiClient.ArtifactDownloadException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    command.getRequestId(),
                    command.getTraceId(),
                    "WORKER_UNAVAILABLE",
                    ex.getMessage(),
                    "ARTIFACT_DOWNLOAD");
            failed.setArtifactReferenceJson(result.getArtifactReferenceJson());
            return failed;
        } catch (JsonProcessingException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    command.getRequestId(),
                    command.getTraceId(),
                    "WORKER_PROTOCOL_FAILURE",
                    ex.getMessage(),
                    "ARTIFACT_DOWNLOAD");
            failed.setArtifactReferenceJson(result.getArtifactReferenceJson());
            return failed;
        } catch (RuntimeException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    command.getRequestId(),
                    command.getTraceId(),
                    "INTERNAL_FAILURE",
                    ex.getMessage(),
                    "STORAGE_PERSIST");
            failed.setArtifactReferenceJson(result.getArtifactReferenceJson());
            return failed;
        }
    }

    private String toStorageResultJson(UploadStorageFacadeResponse response) {
        if (response == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize storage upload response", ex);
        }
    }

    private String toStorageResultJson(CompleteMultipartUploadFacadeResponse response) {
        if (response == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize multipart upload response", ex);
        }
    }

    private UploadStorageFacadeResponse persistSmallArtifact(WorkerAiClient.DownloadedArtifact artifact) {
        return storageFacade.upload(UploadStorageFacadeRequest.builder()
                .inputStream(new ByteArrayInputStream(artifact.data()))
                .originalFilename(artifact.filename())
                .contentType(artifact.contentType())
                .sizeBytes(artifact.sizeBytes())
                .remarks("AI artifact transfer")
                .build());
    }

    private CompleteMultipartUploadFacadeResponse persistMultipartArtifact(WorkerAiClient.DownloadedArtifact artifact) {
        InitMultipartUploadFacadeResponse init =
                storageFacade.initMultipartUpload(InitMultipartUploadFacadeRequest.builder()
                        .originalFilename(artifact.filename())
                        .mimeType(artifact.contentType())
                        .totalSize(artifact.sizeBytes())
                        .partSize(MULTIPART_PART_SIZE_BYTES)
                        .build());
        byte[] data = artifact.data();
        int partNumber = 1;
        for (int offset = 0; offset < data.length; offset += (int) MULTIPART_PART_SIZE_BYTES) {
            int length = Math.min((int) MULTIPART_PART_SIZE_BYTES, data.length - offset);
            byte[] partBytes = new byte[length];
            System.arraycopy(data, offset, partBytes, 0, length);
            storageFacade.uploadPart(UploadMultipartPartFacadeRequest.builder()
                    .uploadId(init.getUploadId())
                    .partNumber(partNumber)
                    .size((long) length)
                    .inputStream(new ByteArrayInputStream(partBytes))
                    .build());
            partNumber++;
        }
        return storageFacade.completeMultipart(CompleteMultipartUploadFacadeRequest.builder()
                .uploadId(init.getUploadId())
                .bucketName(init.getBucketName())
                .objectKey(init.getObjectKey())
                .size(artifact.sizeBytes())
                .build());
    }
}
