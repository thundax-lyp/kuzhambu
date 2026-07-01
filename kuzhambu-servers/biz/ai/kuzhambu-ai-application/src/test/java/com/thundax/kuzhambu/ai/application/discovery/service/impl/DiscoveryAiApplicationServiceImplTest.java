package com.thundax.kuzhambu.ai.application.discovery.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.discovery.support.DiscoveryAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class DiscoveryAiApplicationServiceImplTest {

    private final DiscoveryAiWorkerUsecaseResolver resolver = new DiscoveryAiWorkerUsecaseResolver();

    @Test
    void understandQueryShouldMapToSyncInvokeCommand() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);

        DiscoveryAiResult result = service.understandQuery(request(false));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("discovery", capturedCommand.getScope());
        assertEquals("DISCOVERY_QUERY_UNDERSTANDING", capturedCommand.getOperation());
        assertEquals("/internal/ai/discovery/query-understanding", capturedCommand.getWorkerPath());
        assertEquals("query_understanding", capturedCommand.getCapability());
        assertFalse(capturedCommand.isStream());
        assertFalse(capturedCommand.isCreateCandidate());
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals("query_understanding", result.getCapability());
    }

    @Test
    void streamAnswerShouldUseStreamingInvocation() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);

        DiscoveryAiResult result = service.streamAnswer(request(false));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertTrue(invocationService.streamInvoked());
        assertEquals("DISCOVERY_ANSWER_GENERATION_STREAM", capturedCommand.getOperation());
        assertEquals("/internal/ai/discovery/answer-generation/stream", capturedCommand.getWorkerPath());
        assertEquals("answer_generation", capturedCommand.getCapability());
        assertTrue(capturedCommand.isStream());
        assertFalse(capturedCommand.isCreateCandidate());
        assertEquals("STREAM_SUCCEEDED", result.getStatus());
        assertEquals("answer_generation", result.getCapability());
    }

    @Test
    void generateAnswerShouldExposeFinalFailureStateFromInvocationResult() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        invocationService.failed = true;
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);

        DiscoveryAiResult result = service.generateAnswer(request(false));

        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertEquals("WORKER_STREAM", result.getErrorType());
        assertEquals("stream interrupted", result.getErrorMessage());
    }

    private DiscoveryAiRequest request(boolean stream) {
        return new DiscoveryAiRequest(
                3L,
                "discovery-portal",
                10L,
                "model-a",
                20L,
                "req-1",
                "trace-1",
                "[{\"role\":\"user\",\"content\":\"hello\"}]",
                "{\"locale\":\"zh-CN\"}",
                "hash-a",
                "{\"query\":\"hello\"}",
                "{\"type\":\"object\"}",
                stream,
                true,
                "zh-CN");
    }

    private static class CapturingInvocationService implements AiWorkerInvocationApplicationService {

        private AiInvokeCommand captured;
        private boolean streamInvoked;
        private boolean failed;

        @Override
        public AiInvokeResult invoke(AiInvokeCommand command) {
            captured = command;
            AiInvokeResult result = new AiInvokeResult();
            result.setCallId(101L);
            result.setCandidateId(102L);
            result.setRequestId(command.getRequestId());
            result.setTraceId(command.getTraceId());
            result.setStatus(failed ? "FAILED" : "SUCCEEDED");
            result.setCapability(command.getCapability());
            result.setResultFormat("STRUCTURED");
            result.setResultPayload(failed ? null : "{\"intent\":\"search\"}");
            result.setErrorType(failed ? "WORKER_STREAM" : null);
            result.setErrorMessage(failed ? "stream interrupted" : null);
            return result;
        }

        @Override
        public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
            captured = command;
            streamInvoked = true;
            AiInvokeResult result = new AiInvokeResult();
            result.setCallId(201L);
            result.setCandidateId(null);
            result.setRequestId(command.getRequestId());
            result.setTraceId(command.getTraceId());
            result.setStatus("STREAM_SUCCEEDED");
            result.setCapability(command.getCapability());
            result.setResultFormat("TEXT");
            result.setResultPayload("answer");
            return result;
        }

        private AiInvokeCommand capturedCommand() {
            return captured;
        }

        private boolean streamInvoked() {
            return streamInvoked;
        }
    }
}
