package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway.ArtifactDownloadException;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway.DownloadedArtifact;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
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
    private final AiWorkerGateway aiWorkerGateway;
    private final StorageFacade storageFacade;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public AiWorkerInvocationApplicationServiceImpl(
            AiInvocationRepository aiInvocationRepository,
            AiWorkerGateway aiWorkerGateway,
            StorageFacade storageFacade) {
        this.aiInvocationRepository = aiInvocationRepository;
        this.aiWorkerGateway = aiWorkerGateway;
        this.storageFacade = storageFacade;
    }

    @Override
    public AiInvokeResult invoke(AiInvokeCommand command) {
        validateCommand(command);
        command.setStream(false);
        AiInvocationLog invocationLog = toRunningInvocationLog(command);
        AiCallId callId = aiInvocationRepository.insertInvocationLog(invocationLog);
        invocationLog.setCallId(callId);
        AiInvokeResult result;
        try {
            result = aiWorkerGateway.invoke(command);
        } catch (RuntimeException ex) {
            result = AiInvokeResult.failed(
                    RequestIdCodec.toDomain(command.getRequestId()),
                    TraceIdCodec.toDomain(command.getTraceId()),
                    "WORKER_UNAVAILABLE",
                    ex.getMessage(),
                    "WORKER_REQUEST");
        }
        return completeCall(command, invocationLog, normalizeResult(command, result));
    }

    @Override
    public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        validateCommand(command);
        command.setStream(true);
        AiInvocationLog invocationLog = toRunningInvocationLog(command);
        AiCallId callId = aiInvocationRepository.insertInvocationLog(invocationLog);
        invocationLog.setCallId(callId);
        AtomicReference<AiInvokeResult> completedResult = new AtomicReference<>();
        try {
            aiWorkerGateway.stream(command, event -> handleStreamEvent(eventConsumer, completedResult, event));
        } catch (RuntimeException ex) {
            completedResult.compareAndSet(
                    null,
                    AiInvokeResult.failed(
                            RequestIdCodec.toDomain(command.getRequestId()),
                            TraceIdCodec.toDomain(command.getTraceId()),
                            "WORKER_UNAVAILABLE",
                            ex.getMessage(),
                            "WORKER_STREAM"));
        }
        AiInvokeResult result = completedResult.get();
        if (result == null) {
            result = AiInvokeResult.failed(
                    RequestIdCodec.toDomain(command.getRequestId()),
                    TraceIdCodec.toDomain(command.getTraceId()),
                    "WORKER_PROTOCOL_FAILURE",
                    "Worker stream ended without completed event",
                    "WORKER_STREAM");
        }
        return completeCall(command, invocationLog, normalizeResult(command, result));
    }

    private AiInvocationLog toRunningInvocationLog(AiInvokeCommand command) {
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setBatchId(command.getBatchId());
        invocationLog.setScope(command.getScope());
        invocationLog.setCapability(command.getCapability());
        invocationLog.setContentRef(command.getContentRef());
        invocationLog.setTargetObjectId(command.getTargetObjectId());
        invocationLog.setServiceId(command.getServiceId());
        invocationLog.setServiceRole(command.getServiceRole());
        invocationLog.setModelId(AiModelIdCodec.toDomain(command.getModelId()));
        invocationLog.setModelName(AiModelNameCodec.toDomain(command.getModelName()));
        invocationLog.setPromptVersionId(PromptVersionIdCodec.toDomain(command.getPromptVersionId()));
        invocationLog.setRequestId(RequestIdCodec.toDomain(command.getRequestId()));
        invocationLog.setTraceId(TraceIdCodec.toDomain(command.getTraceId()));
        invocationLog.setStreamUsed(command.isStream());
        invocationLog.setRequestedAt(Instant.now());
        return invocationLog;
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

    private AiInvokeResult completeCall(AiInvokeCommand command, AiInvocationLog invocationLog, AiInvokeResult result) {
        Instant completedAt = Instant.now();
        invocationLog.recordResult(
                result.getResultFormat(),
                result.getResultPayload(),
                result.getArtifactReferenceJson(),
                result.getWarningsJson());
        if (result.isSucceeded()) {
            invocationLog.markSucceeded(result.getUsage(), completedAt);
            aiInvocationRepository.updateInvocationLog(invocationLog);
            if (command.isCreateCandidate()) {
                AiCandidateId candidateId =
                        aiInvocationRepository.insertCandidate(result.toCandidate(command, invocationLog.getCallId()));
                result.setCandidateId(candidateId);
            }
        } else {
            invocationLog.recordFailureStage(result.getFailureStage());
            invocationLog.markFailed(result.getErrorType(), result.getErrorMessage(), result.getUsage(), completedAt);
            if (command.isCreateCandidate() && !command.isStream()) {
                AiCandidate candidate = result.toCandidate(command, invocationLog.getCallId());
                candidate.reject(
                        result.getErrorType(), result.getErrorMessage(), result.getFailureStage(), completedAt);
                AiCandidateId candidateId = aiInvocationRepository.insertCandidate(candidate);
                result.setCandidateId(candidateId);
            }
            aiInvocationRepository.updateInvocationLog(invocationLog);
        }
        result.setCallId(invocationLog.getCallId());
        return result;
    }

    private AiInvokeResult normalizeResult(AiInvokeCommand command, AiInvokeResult result) {
        if (result == null) {
            return AiInvokeResult.failed(
                    RequestIdCodec.toDomain(command.getRequestId()),
                    TraceIdCodec.toDomain(command.getTraceId()),
                    "WORKER_PROTOCOL_FAILURE",
                    "Worker returned empty result",
                    "WORKER_RESULT");
        }
        if (result.getRequestId() == null) {
            result.setRequestId(RequestIdCodec.toDomain(command.getRequestId()));
        }
        if (result.getTraceId() == null) {
            result.setTraceId(TraceIdCodec.toDomain(command.getTraceId()));
        }
        if (command.getCapability() != null) {
            result.setCapability(command.getCapability());
        }
        if (isBlank(result.getResultFormat())) {
            result.setResultFormat(defaultResultFormat(command, result));
        }
        if (result.isSucceeded() && !isBlank(result.getArtifactReferenceJson())) {
            return persistArtifactResult(command, result);
        }
        return result;
    }

    private void validateCommand(AiInvokeCommand command) {
        if (command == null
                || isBlank(command.getScope())
                || command.getCapability() == null
                || isBlank(command.getRequestId())
                || isBlank(command.getTraceId())
                || isBlank(command.getPromptMessagesJson())
                || isBlank(command.getInputPayloadJson())) {
            throw new BizException("AI invoke command is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultResultFormat(AiInvokeCommand command, AiInvokeResult result) {
        if (result != null && !isBlank(result.getArtifactReferenceJson())) {
            return "ARTIFACT";
        }
        return command != null && command.isForceJson() ? "JSON" : "TEXT";
    }

    private AiInvokeResult persistArtifactResult(AiInvokeCommand command, AiInvokeResult result) {
        try {
            JsonNode artifactReference = objectMapper.readTree(result.getArtifactReferenceJson());
            DownloadedArtifact artifact = aiWorkerGateway.downloadArtifact(
                    RequestIdCodec.toDomain(command.getRequestId()),
                    TraceIdCodec.toDomain(command.getTraceId()),
                    artifactReference.path("downloadPath").asText());
            result.setResultPayload(
                    artifact.sizeBytes() > MULTIPART_THRESHOLD_BYTES
                            ? toStorageResultJson(persistMultipartArtifact(artifact))
                            : toStorageResultJson(persistSmallArtifact(artifact)));
            return result;
        } catch (ArtifactDownloadException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    RequestIdCodec.toDomain(command.getRequestId()),
                    TraceIdCodec.toDomain(command.getTraceId()),
                    "WORKER_UNAVAILABLE",
                    ex.getMessage(),
                    "ARTIFACT_DOWNLOAD");
            failed.setArtifactReferenceJson(result.getArtifactReferenceJson());
            return failed;
        } catch (JsonProcessingException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    RequestIdCodec.toDomain(command.getRequestId()),
                    TraceIdCodec.toDomain(command.getTraceId()),
                    "WORKER_PROTOCOL_FAILURE",
                    ex.getMessage(),
                    "ARTIFACT_DOWNLOAD");
            failed.setArtifactReferenceJson(result.getArtifactReferenceJson());
            return failed;
        } catch (RuntimeException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    RequestIdCodec.toDomain(command.getRequestId()),
                    TraceIdCodec.toDomain(command.getTraceId()),
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

    private UploadStorageFacadeResponse persistSmallArtifact(DownloadedArtifact artifact) {
        return storageFacade.upload(UploadStorageFacadeRequest.builder()
                .inputStream(new ByteArrayInputStream(artifact.data()))
                .originalFilename(artifact.filename())
                .contentType(artifact.contentType())
                .sizeBytes(artifact.sizeBytes())
                .remarks("AI artifact transfer")
                .build());
    }

    private CompleteMultipartUploadFacadeResponse persistMultipartArtifact(DownloadedArtifact artifact) {
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
