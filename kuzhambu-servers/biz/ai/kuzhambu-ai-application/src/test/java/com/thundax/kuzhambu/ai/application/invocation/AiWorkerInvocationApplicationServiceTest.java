package com.thundax.kuzhambu.ai.application.invocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService.WorkerAiClient;
import com.thundax.kuzhambu.ai.application.invocation.service.impl.AiWorkerInvocationApplicationServiceImpl;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
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
        };
        AiWorkerInvocationApplicationServiceImpl service =
                new AiWorkerInvocationApplicationServiceImpl(repository, workerClient);

        AiInvokeResult result = service.stream(command(), event -> {});

        assertEquals("FAILED", result.getStatus());
        assertEquals("WORKER_PROTOCOL_FAILURE", result.getErrorType());
        assertEquals("Worker stream ended without completed event", result.getErrorMessage());
        assertEquals(100L, result.getCallId());
        assertEquals("FAILED", repository.updatedCallRecord.get().getStatus());
        assertFalse(repository.updatedCallRecord.get().isStreamCompleted());
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

    private static class RecordingInvocationRepository implements AiInvocationRepository {

        private final AtomicReference<AiCallRecord> updatedCallRecord = new AtomicReference<>();

        @Override
        public AiCallRecord getCallRecord(Long callId) {
            return null;
        }

        @Override
        public Long saveCallRecord(AiCallRecord callRecord) {
            return 100L;
        }

        @Override
        public int updateCallRecord(AiCallRecord callRecord) {
            updatedCallRecord.set(callRecord);
            return 1;
        }

        @Override
        public AiCandidate getCandidate(Long candidateId) {
            return null;
        }

        @Override
        public Long saveCandidate(AiCandidate candidate) {
            return 200L;
        }

        @Override
        public int updateCandidate(AiCandidate candidate) {
            return 0;
        }

        @Override
        public List<AiCandidate> listCandidates(String contentType, Long contentId, String capability, String status) {
            return Collections.emptyList();
        }
    }
}
