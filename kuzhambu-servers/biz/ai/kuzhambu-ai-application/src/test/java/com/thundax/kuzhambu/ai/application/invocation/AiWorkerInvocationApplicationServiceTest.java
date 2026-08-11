package com.thundax.kuzhambu.ai.application.invocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeContext;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeModelConfig;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeOptions;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePayload;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePrompt;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTarget;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTrace;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeWorkerRoute;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway;
import com.thundax.kuzhambu.ai.application.invocation.gateway.AiWorkerGateway.DownloadedArtifact;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.impl.AiWorkerInvocationApplicationServiceImpl;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCallIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiCandidateIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiCandidateStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class AiWorkerInvocationApplicationServiceTest {

    @Test
    void streamShouldFailWhenWorkerEndsWithoutCompletedEvent() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                AiStreamEventResult event = new AiStreamEventResult();
                event.setEventType("delta");
                event.setRequestId(command.trace().requestId());
                event.setTraceId(command.trace().traceId());
                event.setStatus(AiInvocationStatus.RUNNING);
                eventConsumer.accept(event);
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());

        AiInvokeResult result = service.stream(command(), event -> {});

        assertEquals(AiInvocationStatus.FAILED, result.getStatus());
        assertEquals("WORKER_PROTOCOL_FAILURE", result.getErrorType());
        assertEquals("Worker stream ended without completed event", result.getErrorMessage());
        assertEquals(new AiCallId(100L), result.getCallId());
        assertEquals(
                AiInvocationStatus.FAILED, repository.updatedInvocationLog.get().getStatus());
        assertFalse(repository.updatedInvocationLog.get().isStreamCompleted());
    }

    @Test
    void streamShouldNotPersistCandidateWhenWorkerFails() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                AiStreamEventResult event = new AiStreamEventResult();
                event.setEventType("error");
                event.setRequestId(command.trace().requestId());
                event.setTraceId(command.trace().traceId());
                event.setErrorType("WORKER_PROTOCOL_FAILURE");
                event.setErrorMessage("bad stream");
                event.setFailureStage("WORKER_STREAM");
                eventConsumer.accept(event);
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());
        AiInvokeCommand command = withCreateCandidate(command());

        AiInvokeResult result = service.stream(command, event -> {});

        assertEquals(AiInvocationStatus.FAILED, result.getStatus());
        assertEquals(new AiCallId(100L), result.getCallId());
        assertNull(result.getCandidateId());
        assertEquals(
                AiInvocationStatus.FAILED, repository.updatedInvocationLog.get().getStatus());
        assertNull(repository.savedCandidate.get());
    }

    @Test
    void invokeShouldPreserveBusinessCapabilityWhenWorkerReturnsCanonicalCapability() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AtomicReference<AiInvokeCommand> capturedCommand = new AtomicReference<>();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                capturedCommand.set(command);
                AiInvokeResult result = new AiInvokeResult();
                result.setRequestId(command.trace().requestId());
                result.setTraceId(command.trace().traceId());
                result.setStatus(AiInvocationStatus.SUCCEEDED);
                result.setCapability(command.context().capability());
                result.setResultFormat("text");
                result.setResultPayload("candidate-result");
                result.setUsage(AiUsageSnapshot.empty());
                return result;
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());

        AiInvokeCommand command = withCreateCandidate(withRouting(
                command(), AiBusinessCapability.from("CLASSICS_SUMMARY"), "summary", "CLASSICS_SANCAI_SUMMARY"));

        AiInvokeResult result = service.invoke(command);

        assertEquals("CLASSICS_SANCAI_SUMMARY", capturedCommand.get().route().operation());
        assertFalse(capturedCommand.get().options().stream());
        assertEquals(AiBusinessCapability.CLASSICS_SUMMARY, result.getCapability());
        assertEquals(new AiCallId(100L), result.getCallId());
        assertEquals("text", repository.updatedInvocationLog.get().getResultFormat());
        assertEquals("candidate-result", repository.updatedInvocationLog.get().getResultPayload());
        assertEquals(new AiCandidateId(200L), result.getCandidateId());
        assertEquals(100L, repository.savedCandidate.get().getCallId().value());
        assertEquals(
                AiBusinessCapability.CLASSICS_SUMMARY,
                repository.savedCandidate.get().getCapability());
    }

    @Test
    void invokeShouldPersistFailedCandidateSnapshot() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                AiInvokeResult result = new AiInvokeResult();
                result.setRequestId(command.trace().requestId());
                result.setTraceId(command.trace().traceId());
                result.setStatus(AiInvocationStatus.FAILED);
                result.setCapability(command.context().capability());
                result.setResultFormat("TEXT");
                result.setResultPayload("should-be-persisted");
                result.setErrorType("WORKER_PROTOCOL_FAILURE");
                result.setErrorMessage("bad worker response");
                result.setFailureStage("WORKER_RESULT");
                result.setUsage(AiUsageSnapshot.empty());
                return result;
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());

        AiInvokeCommand command = withCreateCandidate(command());
        AiInvokeResult result = service.invoke(command);

        assertEquals(AiInvocationStatus.FAILED, result.getStatus());
        assertEquals(new AiCallId(100L), result.getCallId());
        assertEquals(new AiCandidateId(200L), result.getCandidateId());
        assertEquals(
                AiInvocationStatus.FAILED, repository.updatedInvocationLog.get().getStatus());
        assertEquals(
                "WORKER_PROTOCOL_FAILURE", repository.updatedInvocationLog.get().getErrorType());
        assertEquals("WORKER_RESULT", repository.updatedInvocationLog.get().getFailureStage());
        assertEquals("TEXT", repository.updatedInvocationLog.get().getResultFormat());
        assertEquals(
                "should-be-persisted", repository.updatedInvocationLog.get().getResultPayload());
        assertNotNull(repository.savedCandidate.get());
        assertEquals(AiCandidateStatus.REJECTED, repository.savedCandidate.get().getStatus());
        assertEquals("WORKER_PROTOCOL_FAILURE", repository.savedCandidate.get().getErrorType());
        assertEquals("WORKER_RESULT", repository.savedCandidate.get().getFailureStage());
        assertEquals("TEXT", repository.savedCandidate.get().getResultFormat());
        assertNotNull(repository.savedCandidate.get().getRejectedAt());
    }

    @Test
    void invokeShouldPersistPartialLogAndPendingCandidate() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                return partialResult(command);
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());
        AiInvokeCommand command = withCreateCandidate(command());

        AiInvokeResult result = service.invoke(command);

        assertEquals(AiInvocationStatus.PARTIAL, result.getStatus());
        assertEquals(new AiCandidateId(200L), result.getCandidateId());
        assertPartialPersistence(repository, false);
    }

    @Test
    void streamShouldPersistPartialLogAndPendingCandidate() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                AiInvokeResult partialResult = partialResult(command);
                AiStreamEventResult event = new AiStreamEventResult();
                event.setEventType("completed");
                event.setRequestId(partialResult.getRequestId());
                event.setTraceId(partialResult.getTraceId());
                event.setStatus(partialResult.getStatus());
                event.setResultFormat(partialResult.getResultFormat());
                event.setResultPayload(partialResult.getResultPayload());
                event.setFailureStage(partialResult.getFailureStage());
                event.setErrorType(partialResult.getErrorType());
                event.setErrorMessage(partialResult.getErrorMessage());
                event.setUsage(partialResult.getUsage());
                eventConsumer.accept(event);
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());
        AiInvokeCommand command = withCreateCandidate(command());

        AiInvokeResult result = service.stream(command, event -> {});

        assertEquals(AiInvocationStatus.PARTIAL, result.getStatus());
        assertEquals(new AiCandidateId(200L), result.getCandidateId());
        assertPartialPersistence(repository, true);
    }

    @Test
    void invokeShouldPersistPartialArtifactBeforeCreatingCandidate() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                AiInvokeResult result = partialResult(command);
                result.setArtifactReferenceJson(
                        "{\"downloadPath\":\"/internal/artifacts/art-1\",\"filename\":\"partial.png\"}");
                return result;
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                return new DownloadedArtifact(
                        "image".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        "image/png",
                        "partial.png",
                        "sha256",
                        5L,
                        null);
            }
        };
        AiWorkerInvocationApplicationServiceImpl service = new AiWorkerInvocationApplicationServiceImpl(
                repository, aiWorkerGateway, storageFacadeReturningUpload());
        AiInvokeCommand command = withCreateCandidate(command());

        AiInvokeResult result = service.invoke(command);

        assertEquals(AiInvocationStatus.PARTIAL, result.getStatus());
        assertTrue(result.getResultPayload().contains("\"storageObjectId\":901"));
        assertEquals("PARTIAL_RESULT", result.getErrorType());
        assertEquals(AiCandidateStatus.PENDING, repository.savedCandidate.get().getStatus());
        assertEquals(result.getResultPayload(), repository.savedCandidate.get().getResultPayload());
    }

    @Test
    void invokeShouldDefaultResultFormatWhenWorkerOmitsFormat() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                AiInvokeResult result = new AiInvokeResult();
                result.setRequestId(command.trace().requestId());
                result.setTraceId(command.trace().traceId());
                result.setStatus(AiInvocationStatus.SUCCEEDED);
                result.setCapability(command.context().capability());
                result.setResultPayload("{\"ok\":true}");
                result.setUsage(AiUsageSnapshot.empty());
                return result;
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());

        AiInvokeCommand command =
                withOutputContract(command(), command().context().capability(), true, null);
        AiInvokeResult result = service.invoke(command);

        assertEquals(AiInvocationStatus.SUCCEEDED, result.getStatus());
        assertEquals("JSON", result.getResultFormat());
        assertEquals("JSON", repository.updatedInvocationLog.get().getResultFormat());
        assertEquals("JSON", repository.savedCandidate.get().getResultFormat());
    }

    @Test
    void invokeShouldRejectStructuredPayloadWhenRequiredArrayFieldHasWrongType() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                AiInvokeResult result = new AiInvokeResult();
                result.setRequestId(command.trace().requestId());
                result.setTraceId(command.trace().traceId());
                result.setStatus(AiInvocationStatus.SUCCEEDED);
                result.setCapability(command.context().capability());
                result.setResultFormat("STRUCTURED");
                result.setResultPayload("{\"entities\":{\"name\":\"礼\"},\"relations\":[]}");
                result.setUsage(AiUsageSnapshot.empty());
                return result;
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());

        AiInvokeCommand command = withOutputContract(
                command(),
                AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT,
                true,
                "{\"type\":\"object\",\"properties\":{\"entities\":{\"type\":\"array\",\"items\":{\"type\":\"object\"}},\"relations\":{\"type\":\"array\",\"items\":{\"type\":\"object\"}}},\"required\":[\"entities\",\"relations\"]}");
        AiInvokeResult result = service.invoke(command);

        assertEquals(AiInvocationStatus.FAILED, result.getStatus());
        assertEquals("OUTPUT_FORMAT_FAILURE", result.getErrorType());
        assertEquals("WORKER_RESULT", result.getFailureStage());
        assertEquals(
                AiInvocationStatus.FAILED, repository.updatedInvocationLog.get().getStatus());
        assertEquals(
                "OUTPUT_FORMAT_FAILURE", repository.updatedInvocationLog.get().getErrorType());
        assertEquals(AiCandidateStatus.REJECTED, repository.savedCandidate.get().getStatus());
        assertEquals("OUTPUT_FORMAT_FAILURE", repository.savedCandidate.get().getErrorType());
    }

    @Test
    void invokeShouldRejectStructuredPayloadWhenArrayHasTooFewItems() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                AiInvokeResult result = new AiInvokeResult();
                result.setRequestId(command.trace().requestId());
                result.setTraceId(command.trace().traceId());
                result.setStatus(AiInvocationStatus.SUCCEEDED);
                result.setCapability(command.context().capability());
                result.setResultFormat("STRUCTURED");
                result.setResultPayload("{\"tags\":[]}");
                result.setUsage(AiUsageSnapshot.empty());
                return result;
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());

        AiInvokeCommand command = withOutputContract(
                command(),
                AiBusinessCapability.CLASSICS_TAG_EXTRACT,
                true,
                "{\"type\":\"object\",\"properties\":{\"tags\":{\"type\":\"array\",\"minItems\":3,\"maxItems\":8,\"items\":{\"type\":\"object\"}}},\"required\":[\"tags\"]}");
        AiInvokeResult result = service.invoke(command);

        assertEquals(AiInvocationStatus.FAILED, result.getStatus());
        assertEquals("OUTPUT_FORMAT_FAILURE", result.getErrorType());
        assertEquals("WORKER_RESULT", result.getFailureStage());
        assertEquals(
                AiInvocationStatus.FAILED, repository.updatedInvocationLog.get().getStatus());
        assertEquals(AiCandidateStatus.REJECTED, repository.savedCandidate.get().getStatus());
    }

    @Test
    void invokeShouldRejectStructuredPayloadWhenArrayHasTooManyItems() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AiWorkerGateway aiWorkerGateway = new AiWorkerGateway() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                AiInvokeResult result = new AiInvokeResult();
                result.setRequestId(command.trace().requestId());
                result.setTraceId(command.trace().traceId());
                result.setStatus(AiInvocationStatus.SUCCEEDED);
                result.setCapability(command.context().capability());
                result.setResultFormat("STRUCTURED");
                result.setResultPayload("{\"qaPairs\":[{},{},{},{},{},{}]}");
                result.setUsage(AiUsageSnapshot.empty());
                return result;
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public DownloadedArtifact downloadArtifact(RequestId requestId, TraceId traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, aiWorkerGateway, unusedStorageFacade());

        AiInvokeCommand command = withOutputContract(
                command(),
                AiBusinessCapability.CLASSICS_QA,
                true,
                "{\"type\":\"object\",\"properties\":{\"qaPairs\":{\"type\":\"array\",\"minItems\":3,\"maxItems\":5,\"items\":{\"type\":\"object\"}}},\"required\":[\"qaPairs\"]}");
        AiInvokeResult result = service.invoke(command);

        assertEquals(AiInvocationStatus.FAILED, result.getStatus());
        assertEquals("OUTPUT_FORMAT_FAILURE", result.getErrorType());
        assertEquals("WORKER_RESULT", result.getFailureStage());
        assertEquals(
                AiInvocationStatus.FAILED, repository.updatedInvocationLog.get().getStatus());
        assertEquals(AiCandidateStatus.REJECTED, repository.savedCandidate.get().getStatus());
    }

    private AiInvokeCommand command() {
        return new AiInvokeCommand(
                new AiInvokeContext(null, "classics", AiBusinessCapability.from("CLASSICS_TRANSLATE")),
                new AiInvokeWorkerRoute(null, "translate", null),
                new AiInvokeTarget(AiContentRef.ofNullable("entry", 10L), null),
                new AiInvokeModelConfig(null, null, new AiModelId(20L), AiModelName.of("model-a")),
                new AiInvokeTrace(new RequestId("req-1"), new TraceId("trace-1")),
                new AiInvokePrompt(null, "[{\"role\":\"user\",\"content\":\"hello\"}]", null, null),
                new AiInvokePayload("{\"text\":\"hello\"}", null),
                new AiInvokeOptions(false, false, null, false, false));
    }

    private AiInvokeCommand withCreateCandidate(AiInvokeCommand command) {
        return copyCommand(
                command,
                command.context().capability(),
                command.route().workerCapability(),
                command.route().operation(),
                command.payload().outputSchemaJson(),
                command.options().forceJson(),
                true);
    }

    private AiInvokeCommand withRouting(
            AiInvokeCommand command, AiBusinessCapability capability, String workerCapability, String operation) {
        return copyCommand(
                command,
                capability,
                workerCapability,
                operation,
                command.payload().outputSchemaJson(),
                command.options().forceJson(),
                command.options().createCandidate());
    }

    private AiInvokeCommand withOutputContract(
            AiInvokeCommand command, AiBusinessCapability capability, boolean forceJson, String outputSchemaJson) {
        return copyCommand(
                command,
                capability,
                command.route().workerCapability(),
                command.route().operation(),
                outputSchemaJson,
                forceJson,
                true);
    }

    private AiInvokeCommand copyCommand(
            AiInvokeCommand command,
            AiBusinessCapability capability,
            String workerCapability,
            String operation,
            String outputSchemaJson,
            boolean forceJson,
            boolean createCandidate) {
        return new AiInvokeCommand(
                new AiInvokeContext(
                        command.context().batchId(), command.context().scope(), capability),
                new AiInvokeWorkerRoute(
                        workerCapability, operation, command.route().workerPath()),
                command.target(),
                command.modelConfig(),
                command.trace(),
                command.prompt(),
                new AiInvokePayload(command.payload().inputPayloadJson(), outputSchemaJson),
                new AiInvokeOptions(
                        command.options().stream(),
                        forceJson,
                        command.options().locale(),
                        command.options().allowFallback(),
                        createCandidate));
    }

    private AiInvokeResult partialResult(AiInvokeCommand command) {
        AiInvokeResult result = new AiInvokeResult();
        result.setRequestId(command.trace().requestId());
        result.setTraceId(command.trace().traceId());
        result.setStatus(AiInvocationStatus.PARTIAL);
        result.setCapability(command.context().capability());
        result.setResultFormat("TEXT");
        result.setResultPayload("partial result");
        result.setFailureStage("WORKER_RESULT");
        result.setErrorType("PARTIAL_RESULT");
        result.setErrorMessage("部分结果可用");
        result.setUsage(AiUsageSnapshot.empty());
        return result;
    }

    private void assertPartialPersistence(RecordingInvocationRepository repository, boolean streamCompleted) {
        AiInvocationLog invocationLog = repository.updatedInvocationLog.get();
        assertEquals(AiInvocationStatus.PARTIAL, invocationLog.getStatus());
        assertEquals("partial result", invocationLog.getResultPayload());
        assertEquals("WORKER_RESULT", invocationLog.getFailureStage());
        assertEquals("PARTIAL_RESULT", invocationLog.getErrorType());
        assertEquals("部分结果可用", invocationLog.getErrorMessage());
        assertEquals(streamCompleted, invocationLog.isStreamCompleted());
        assertNotNull(repository.savedCandidate.get());
        assertEquals(AiCandidateStatus.PENDING, repository.savedCandidate.get().getStatus());
        assertEquals("partial result", repository.savedCandidate.get().getResultPayload());
        assertNull(repository.savedCandidate.get().getRejectedAt());
    }

    private static StorageFacade unusedStorageFacade() {
        return (StorageFacade) Proxy.newProxyInstance(
                StorageFacade.class.getClassLoader(),
                new Class<?>[] {StorageFacade.class},
                (proxy, method, args) -> boolean.class.equals(method.getReturnType()) ? Boolean.FALSE : null);
    }

    private static StorageFacade storageFacadeReturningUpload() {
        return (StorageFacade) Proxy.newProxyInstance(
                StorageFacade.class.getClassLoader(), new Class<?>[] {StorageFacade.class}, (proxy, method, args) -> {
                    if ("upload".equals(method.getName())) {
                        return UploadStorageFacadeResponse.builder()
                                .storageObjectId(901L)
                                .originalFilename("partial.png")
                                .contentType("image/png")
                                .sizeBytes(5L)
                                .build();
                    }
                    return boolean.class.equals(method.getReturnType()) ? Boolean.FALSE : null;
                });
    }

    private static class RecordingInvocationRepository implements AiInvocationRepository {

        private final AtomicReference<AiInvocationLog> updatedInvocationLog = new AtomicReference<>();
        private final AtomicReference<AiCandidate> savedCandidate = new AtomicReference<>();

        @Override
        public AiInvocationLog getInvocationLogById(AiCallId callId) {
            return null;
        }

        @Override
        public AiCallId insertInvocationLog(AiInvocationLog invocationLog) {
            return AiCallIdCodec.toDomain(100L);
        }

        @Override
        public int updateInvocationLog(AiInvocationLog invocationLog) {
            updatedInvocationLog.set(invocationLog);
            return 1;
        }

        @Override
        public List<AiInvocationLog> listInvocationLogs(
                java.time.Instant requestedAtStart, java.time.Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public List<AiInvocationLog> listInvocationLogsByBatch(
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId batchId) {
            return Collections.emptyList();
        }

        @Override
        public PageResult<AiInvocationLog> pageInvocationLogsByFilter(
                String scope,
                AiBusinessCapability capability,
                AiContentRef contentRef,
                AiInvocationStatus status,
                String serviceRole,
                AiModelName modelName,
                Boolean fallbackUsed,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, Collections.emptyList());
        }

        @Override
        public List<AiInvocationLog> listInvocationLogs(
                String scope,
                AiBusinessCapability capability,
                String serviceRole,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public AiCandidate getCandidateById(AiCandidateId candidateId) {
            return null;
        }

        @Override
        public AiCandidateId insertCandidate(AiCandidate candidate) {
            savedCandidate.set(candidate);
            return AiCandidateIdCodec.toDomain(200L);
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            return 0;
        }

        @Override
        public List<AiCandidate> listCandidates(
                AiContentRef contentRef,
                AiTargetObjectId targetObjectId,
                AiBusinessCapability capability,
                AiCandidateStatus status) {
            return Collections.emptyList();
        }

        @Override
        public List<AiCandidate> listCandidatesByBatch(
                com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId batchId) {
            return Collections.emptyList();
        }
    }
}
