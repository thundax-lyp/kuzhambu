package com.thundax.kuzhambu.ai.application.invocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService.DownloadedArtifact;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService.WorkerAiClient;
import com.thundax.kuzhambu.ai.application.invocation.service.impl.AiWorkerInvocationApplicationServiceImpl;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
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
        WorkerAiClient workerClient = new WorkerAiClient() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                AiStreamEventResult event = new AiStreamEventResult();
                event.setEventType("delta");
                event.setRequestId(command.getRequestId());
                event.setTraceId(command.getTraceId());
                event.setStatus("RUNNING");
                eventConsumer.accept(event);
            }

            @Override
            public DownloadedArtifact downloadArtifact(String requestId, String traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, workerClient, unusedStorageFacade());

        AiInvokeResult result = service.stream(command(), event -> {});

        assertEquals("FAILED", result.getStatus());
        assertEquals("WORKER_PROTOCOL_FAILURE", result.getErrorType());
        assertEquals("Worker stream ended without completed event", result.getErrorMessage());
        assertEquals(100L, result.getCallId());
        assertEquals("FAILED", repository.updatedCallRecord.get().getStatus());
        assertFalse(repository.updatedCallRecord.get().isStreamCompleted());
    }

    @Test
    void streamShouldNotPersistCandidateWhenWorkerFails() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        WorkerAiClient workerClient = new WorkerAiClient() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                throw new UnsupportedOperationException("not used");
            }

            @Override
            public void stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
                AiStreamEventResult event = new AiStreamEventResult();
                event.setEventType("error");
                event.setRequestId(command.getRequestId());
                event.setTraceId(command.getTraceId());
                event.setErrorType("WORKER_PROTOCOL_FAILURE");
                event.setErrorMessage("bad stream");
                event.setFailureStage("WORKER_STREAM");
                eventConsumer.accept(event);
            }

            @Override
            public DownloadedArtifact downloadArtifact(String requestId, String traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, workerClient, unusedStorageFacade());
        AiInvokeCommand command = command();
        command.setCreateCandidate(true);

        AiInvokeResult result = service.stream(command, event -> {});

        assertEquals("FAILED", result.getStatus());
        assertEquals(100L, result.getCallId());
        assertNull(result.getCandidateId());
        assertEquals("FAILED", repository.updatedCallRecord.get().getStatus());
        assertNull(repository.savedCandidate.get());
    }

    @Test
    void invokeShouldPreserveBusinessCapabilityWhenWorkerReturnsCanonicalCapability() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        AtomicReference<AiInvokeCommand> capturedCommand = new AtomicReference<>();
        WorkerAiClient workerClient = new WorkerAiClient() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                capturedCommand.set(command);
                AiInvokeResult result = new AiInvokeResult();
                result.setRequestId(command.getRequestId());
                result.setTraceId(command.getTraceId());
                result.setStatus("SUCCEEDED");
                result.setCapability(command.getWorkerCapability());
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
            public DownloadedArtifact downloadArtifact(String requestId, String traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, workerClient, unusedStorageFacade());

        AiInvokeCommand command = command();
        command.setOperation("CLASSICS_SANCAI_SUMMARY");
        command.setCapability("classics_summary");
        command.setWorkerCapability("summary");
        command.setCreateCandidate(true);

        AiInvokeResult result = service.invoke(command);

        assertEquals("CLASSICS_SANCAI_SUMMARY", capturedCommand.get().getOperation());
        assertFalse(capturedCommand.get().isStream());
        assertEquals("classics_summary", result.getCapability());
        assertEquals(100L, result.getCallId());
        assertEquals("text", repository.updatedCallRecord.get().getResultFormat());
        assertEquals("candidate-result", repository.updatedCallRecord.get().getResultPayload());
        assertEquals(200L, result.getCandidateId());
        assertEquals(100L, repository.savedCandidate.get().getCallId());
        assertEquals("classics_summary", repository.savedCandidate.get().getCapability());
    }

    @Test
    void invokeShouldPersistFailedCandidateSnapshot() {
        RecordingInvocationRepository repository = new RecordingInvocationRepository();
        WorkerAiClient workerClient = new WorkerAiClient() {
            @Override
            public AiInvokeResult invoke(AiInvokeCommand command) {
                AiInvokeResult result = new AiInvokeResult();
                result.setRequestId(command.getRequestId());
                result.setTraceId(command.getTraceId());
                result.setStatus("FAILED");
                result.setCapability(command.getCapability());
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
            public DownloadedArtifact downloadArtifact(String requestId, String traceId, String downloadPath) {
                throw new UnsupportedOperationException("not used");
            }
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, workerClient, unusedStorageFacade());

        AiInvokeCommand command = command();
        command.setCreateCandidate(true);
        AiInvokeResult result = service.invoke(command);

        assertEquals("FAILED", result.getStatus());
        assertEquals(100L, result.getCallId());
        assertEquals(200L, result.getCandidateId());
        assertEquals("FAILED", repository.updatedCallRecord.get().getStatus());
        assertEquals(
                "WORKER_PROTOCOL_FAILURE", repository.updatedCallRecord.get().getErrorType());
        assertEquals("WORKER_RESULT", repository.updatedCallRecord.get().getFailureStage());
        assertEquals("TEXT", repository.updatedCallRecord.get().getResultFormat());
        assertEquals("should-be-persisted", repository.updatedCallRecord.get().getResultPayload());
        assertNotNull(repository.savedCandidate.get());
        assertEquals("REJECTED", repository.savedCandidate.get().getStatus());
        assertEquals("WORKER_PROTOCOL_FAILURE", repository.savedCandidate.get().getErrorType());
        assertEquals("WORKER_RESULT", repository.savedCandidate.get().getFailureStage());
        assertEquals("TEXT", repository.savedCandidate.get().getResultFormat());
        assertNotNull(repository.savedCandidate.get().getRejectedAt());
    }

    private AiInvokeCommand command() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability("translate");
        command.setOperation("translate");
        command.setContentType("entry");
        command.setContentId(10L);
        command.setModelId(20L);
        command.setModelName("model-a");
        command.setRequestId("req-1");
        command.setTraceId("trace-1");
        command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"hello\"}]");
        command.setInputPayloadJson("{\"text\":\"hello\"}");
        command.setCreateCandidate(false);
        return command;
    }

    private static StorageFacade unusedStorageFacade() {
        return (StorageFacade) Proxy.newProxyInstance(
                StorageFacade.class.getClassLoader(),
                new Class<?>[] {StorageFacade.class},
                (proxy, method, args) -> boolean.class.equals(method.getReturnType()) ? Boolean.FALSE : null);
    }

    private static class RecordingInvocationRepository implements AiInvocationRepository {

        private final AtomicReference<AiCallRecord> updatedCallRecord = new AtomicReference<>();
        private final AtomicReference<AiCandidate> savedCandidate = new AtomicReference<>();

        @Override
        public AiCallRecord getCallRecord(Long callId) {
            return null;
        }

        @Override
        public Long insertCallRecord(AiCallRecord callRecord) {
            return 100L;
        }

        @Override
        public int updateCallRecord(AiCallRecord callRecord) {
            updatedCallRecord.set(callRecord);
            return 1;
        }

        @Override
        public List<AiCallRecord> listCallRecords(
                java.time.Instant requestedAtStart, java.time.Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public PageResult<AiCallRecord> pageCallRecords(
                String scope,
                String capability,
                String contentType,
                Long contentId,
                String status,
                String serviceRole,
                String modelName,
                Boolean fallbackUsed,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, Collections.emptyList());
        }

        @Override
        public List<AiCallRecord> listCallRecords(
                String scope,
                String capability,
                String serviceRole,
                java.time.Instant requestedAtStart,
                java.time.Instant requestedAtEnd) {
            return Collections.emptyList();
        }

        @Override
        public AiCandidate getCandidate(Long candidateId) {
            return null;
        }

        @Override
        public Long insertCandidate(AiCandidate candidate) {
            savedCandidate.set(candidate);
            return 200L;
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            return 0;
        }

        @Override
        public List<AiCandidate> listCandidates(
                String contentType, Long contentId, Long objectId, String capability, String status) {
            return Collections.emptyList();
        }
    }
}
