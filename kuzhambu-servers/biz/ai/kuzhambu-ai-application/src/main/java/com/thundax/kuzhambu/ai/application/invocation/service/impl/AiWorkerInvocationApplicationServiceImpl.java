package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeOptions;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway.ArtifactDownloadException;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway.DownloadedArtifact;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
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
import java.util.ArrayList;
import java.util.List;
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
        command = withStream(command, false);
        AiInvocationLog invocationLog = toRunningInvocationLog(command);
        AiCallId callId = aiInvocationRepository.insertInvocationLog(invocationLog);
        invocationLog.setCallId(callId);
        AiInvokeResult result;
        try {
            result = aiWorkerGateway.invoke(command);
        } catch (RuntimeException ex) {
            result = AiInvokeResult.failed(
                    command.trace().requestId(),
                    command.trace().traceId(),
                    "WORKER_UNAVAILABLE",
                    ex.getMessage(),
                    "WORKER_REQUEST");
        }
        return completeCall(command, invocationLog, normalizeResult(command, result));
    }

    @Override
    public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
        validateCommand(command);
        command = withStream(command, true);
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
                            command.trace().requestId(),
                            command.trace().traceId(),
                            "WORKER_UNAVAILABLE",
                            ex.getMessage(),
                            "WORKER_STREAM"));
        }
        AiInvokeResult result = completedResult.get();
        if (result == null) {
            result = AiInvokeResult.failed(
                    command.trace().requestId(),
                    command.trace().traceId(),
                    "WORKER_PROTOCOL_FAILURE",
                    "Worker stream ended without completed event",
                    "WORKER_STREAM");
        }
        return completeCall(command, invocationLog, normalizeResult(command, result));
    }

    private AiInvocationLog toRunningInvocationLog(AiInvokeCommand command) {
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setBatchId(command.context().batchId());
        invocationLog.setScope(command.context().scope());
        invocationLog.setCapability(command.context().capability());
        invocationLog.setContentRef(command.target().contentRef());
        invocationLog.setTargetObjectId(command.target().targetObjectId());
        invocationLog.setServiceId(command.modelConfig().serviceId());
        invocationLog.setServiceRole(command.modelConfig().serviceRole());
        invocationLog.setModelId(command.modelConfig().modelId());
        invocationLog.setModelName(command.modelConfig().modelName());
        invocationLog.setPromptVersionId(command.prompt().promptVersionId());
        invocationLog.setRequestId(command.trace().requestId());
        invocationLog.setTraceId(command.trace().traceId());
        invocationLog.setStreamUsed(command.options().stream());
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
            if (command.options().createCandidate()) {
                AiCandidateId candidateId =
                        aiInvocationRepository.insertCandidate(result.toCandidate(command, invocationLog.getCallId()));
                result.setCandidateId(candidateId);
            }
        } else if (result.getStatus() == AiInvocationStatus.PARTIAL) {
            invocationLog.recordFailureStage(result.getFailureStage());
            invocationLog.markPartial(result.getErrorType(), result.getErrorMessage(), result.getUsage(), completedAt);
            aiInvocationRepository.updateInvocationLog(invocationLog);
            if (command.options().createCandidate()) {
                AiCandidateId candidateId =
                        aiInvocationRepository.insertCandidate(result.toCandidate(command, invocationLog.getCallId()));
                result.setCandidateId(candidateId);
            }
        } else {
            invocationLog.recordFailureStage(result.getFailureStage());
            invocationLog.markFailed(result.getErrorType(), result.getErrorMessage(), result.getUsage(), completedAt);
            if (command.options().createCandidate() && !command.options().stream()) {
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
                    command.trace().requestId(),
                    command.trace().traceId(),
                    "WORKER_PROTOCOL_FAILURE",
                    "Worker returned empty result",
                    "WORKER_RESULT");
        }
        if (result.getRequestId() == null) {
            result.setRequestId(command.trace().requestId());
        }
        if (result.getTraceId() == null) {
            result.setTraceId(command.trace().traceId());
        }
        if (command.context().capability() != null) {
            result.setCapability(command.context().capability());
        }
        if (isBlank(result.getResultFormat())) {
            result.setResultFormat(defaultResultFormat(command, result));
        }
        result = validateStructuredResult(command, result);
        if ((result.isSucceeded() || result.getStatus() == AiInvocationStatus.PARTIAL)
                && !isBlank(result.getArtifactReferenceJson())) {
            return persistArtifactResult(command, result);
        }
        return result;
    }

    private void validateCommand(AiInvokeCommand command) {
        if (command == null
                || isBlank(command.context().scope())
                || command.context().capability() == null
                || command.trace().requestId() == null
                || command.trace().traceId() == null
                || isBlank(command.prompt().promptMessagesJson())
                || isBlank(command.payload().inputPayloadJson())) {
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
        return command != null && command.options().forceJson() ? "JSON" : "TEXT";
    }

    private AiInvokeResult validateStructuredResult(AiInvokeCommand command, AiInvokeResult result) {
        if (command == null
                || result == null
                || (!result.isSucceeded() && result.getStatus() != AiInvocationStatus.PARTIAL)
                || !couldRequireStructuredValidation(command, result)) {
            return result;
        }
        try {
            JsonNode schema = isBlank(command.payload().outputSchemaJson())
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(command.payload().outputSchemaJson());
            if (!shouldValidateStructuredResult(command, result, schema)) {
                return result;
            }
            JsonNode payload = objectMapper.readTree(extractJsonPayload(result.getResultPayload()));
            validateBySchema(payload, schema, "payload");
            result.setResultPayload(objectMapper.writeValueAsString(payload));
            return result;
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return AiInvokeResult.failed(
                    command.trace().requestId(),
                    command.trace().traceId(),
                    "OUTPUT_FORMAT_FAILURE",
                    ex.getMessage(),
                    "WORKER_RESULT");
        }
    }

    private boolean couldRequireStructuredValidation(AiInvokeCommand command, AiInvokeResult result) {
        return "STRUCTURED".equalsIgnoreCase(result.getResultFormat())
                || command.options().forceJson()
                || !isBlank(command.payload().outputSchemaJson());
    }

    private boolean shouldValidateStructuredResult(AiInvokeCommand command, AiInvokeResult result, JsonNode schema) {
        return "STRUCTURED".equalsIgnoreCase(result.getResultFormat())
                || command.options().forceJson()
                || !"text".equalsIgnoreCase(schema.path("type").asText(""));
    }

    private String extractJsonPayload(String payload) {
        if (isBlank(payload)) {
            throw new IllegalArgumentException("Worker structured output is empty");
        }
        String trimmed = stripMarkdownJsonFence(payload.trim());
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return trimmed;
        }
        int objectStart = trimmed.indexOf('{');
        int arrayStart = trimmed.indexOf('[');
        int start = objectStart < 0 ? arrayStart : arrayStart < 0 ? objectStart : Math.min(objectStart, arrayStart);
        int end = Math.max(trimmed.lastIndexOf('}'), trimmed.lastIndexOf(']'));
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        throw new IllegalArgumentException("Worker structured output is not JSON");
    }

    private String stripMarkdownJsonFence(String payload) {
        if (!payload.startsWith("```")) {
            return payload;
        }
        int firstLineEnd = payload.indexOf('\n');
        int fenceEnd = payload.lastIndexOf("```");
        if (firstLineEnd < 0 || fenceEnd <= firstLineEnd) {
            return payload;
        }
        return payload.substring(firstLineEnd + 1, fenceEnd).trim();
    }

    private void validateBySchema(JsonNode value, JsonNode schema, String path) {
        if (schema == null || schema.isMissingNode() || schema.isNull()) {
            return;
        }
        String type = schema.path("type").asText("");
        if ("text".equalsIgnoreCase(type)) {
            return;
        }
        if ("object".equalsIgnoreCase(type)) {
            validateObject(value, schema, path);
        } else if ("array".equalsIgnoreCase(type)) {
            validateArray(value, schema, path);
        } else if ("string".equalsIgnoreCase(type)) {
            require(value != null && value.isTextual(), path + " must be a string");
        } else if ("number".equalsIgnoreCase(type)) {
            require(value != null && value.isNumber(), path + " must be a number");
        } else if ("integer".equalsIgnoreCase(type)) {
            require(value != null && value.isIntegralNumber(), path + " must be an integer");
        } else if ("boolean".equalsIgnoreCase(type)) {
            require(value != null && value.isBoolean(), path + " must be a boolean");
        }
    }

    private void validateObject(JsonNode value, JsonNode schema, String path) {
        require(value != null && value.isObject(), path + " must be an object");
        for (String fieldName : requiredFields(schema)) {
            require(value.has(fieldName) && !value.get(fieldName).isNull(), path + "." + fieldName + " is required");
        }
        JsonNode properties = schema.path("properties");
        if (!properties.isObject()) {
            return;
        }
        properties.fields().forEachRemaining(entry -> {
            if (value.has(entry.getKey()) && !value.get(entry.getKey()).isNull()) {
                validateBySchema(value.get(entry.getKey()), entry.getValue(), path + "." + entry.getKey());
            }
        });
    }

    private void validateArray(JsonNode value, JsonNode schema, String path) {
        require(value != null && value.isArray(), path + " must be an array");
        if (schema.has("minItems") && schema.get("minItems").canConvertToInt()) {
            int minItems = schema.get("minItems").asInt();
            require(value.size() >= minItems, path + " must contain at least " + minItems + " items");
        }
        if (schema.has("maxItems") && schema.get("maxItems").canConvertToInt()) {
            int maxItems = schema.get("maxItems").asInt();
            require(value.size() <= maxItems, path + " must contain at most " + maxItems + " items");
        }
        JsonNode itemSchema = schema.path("items");
        if (itemSchema.isMissingNode() || itemSchema.isNull()) {
            return;
        }
        int index = 0;
        for (JsonNode item : value) {
            validateBySchema(item, itemSchema, path + "[" + index + "]");
            index++;
        }
    }

    private List<String> requiredFields(JsonNode schema) {
        JsonNode required = schema.path("required");
        List<String> fields = new ArrayList<>();
        if (!required.isArray()) {
            return fields;
        }
        for (JsonNode item : required) {
            if (item.isTextual()) {
                fields.add(item.asText());
            }
        }
        return fields;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private AiInvokeResult persistArtifactResult(AiInvokeCommand command, AiInvokeResult result) {
        try {
            JsonNode artifactReference = objectMapper.readTree(result.getArtifactReferenceJson());
            DownloadedArtifact artifact = aiWorkerGateway.downloadArtifact(
                    command.trace().requestId(),
                    command.trace().traceId(),
                    artifactReference.path("downloadPath").asText());
            result.setResultPayload(
                    artifact.sizeBytes() > MULTIPART_THRESHOLD_BYTES
                            ? toStorageResultJson(persistMultipartArtifact(artifact))
                            : toStorageResultJson(persistSmallArtifact(artifact)));
            return result;
        } catch (ArtifactDownloadException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    command.trace().requestId(),
                    command.trace().traceId(),
                    "WORKER_UNAVAILABLE",
                    ex.getMessage(),
                    "ARTIFACT_DOWNLOAD");
            failed.setArtifactReferenceJson(result.getArtifactReferenceJson());
            return failed;
        } catch (JsonProcessingException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    command.trace().requestId(),
                    command.trace().traceId(),
                    "WORKER_PROTOCOL_FAILURE",
                    ex.getMessage(),
                    "ARTIFACT_DOWNLOAD");
            failed.setArtifactReferenceJson(result.getArtifactReferenceJson());
            return failed;
        } catch (RuntimeException ex) {
            AiInvokeResult failed = AiInvokeResult.failed(
                    command.trace().requestId(),
                    command.trace().traceId(),
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

    private AiInvokeCommand withStream(AiInvokeCommand command, boolean stream) {
        return new AiInvokeCommand(
                command.context(),
                command.route(),
                command.target(),
                command.modelConfig(),
                command.trace(),
                command.prompt(),
                command.payload(),
                new AiInvokeOptions(
                        stream,
                        command.options().forceJson(),
                        command.options().locale(),
                        command.options().allowFallback(),
                        command.options().createCandidate()));
    }
}
