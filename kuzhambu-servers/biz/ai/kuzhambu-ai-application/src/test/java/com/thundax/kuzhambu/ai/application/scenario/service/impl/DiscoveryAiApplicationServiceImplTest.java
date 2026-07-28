package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.DiscoveryAiCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.DiscoveryAiInvokeResult;
import com.thundax.kuzhambu.ai.application.scenario.support.DiscoveryAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class DiscoveryAiApplicationServiceImplTest {

    private final DiscoveryAiWorkerUsecaseResolver resolver = new DiscoveryAiWorkerUsecaseResolver();

    @Test
    void understandQueryShouldMapToSyncInvokeCommand() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);

        DiscoveryAiInvokeResult result = service.understandQuery(command(false));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("discovery", capturedCommand.getScope());
        assertEquals("DISCOVERY_QUERY_UNDERSTANDING", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.DISCOVERY_QUERY_UNDERSTANDING, capturedCommand.getCapability());
        assertEquals("query_understanding", capturedCommand.getWorkerCapability());
        assertEquals("DISCOVERY_QUERY", capturedCommand.getContentRef().contentType());
        assertFalse(capturedCommand.isStream());
        assertFalse(capturedCommand.isCreateCandidate());
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals(AiBusinessCapability.DISCOVERY_QUERY_UNDERSTANDING.value(), result.getCapability());
    }

    @Test
    void streamAnswerShouldUseStreamingInvocation() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);
        List<String> deltas = new ArrayList<>();

        DiscoveryAiInvokeResult result =
                service.streamAnswer(command(false), event -> deltas.add(event.getDeltaText()));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertTrue(invocationService.streamInvoked());
        assertEquals(List.of("王圻", "文档答案"), deltas);
        assertEquals("DISCOVERY_ANSWER_GENERATION_STREAM", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION, capturedCommand.getCapability());
        assertEquals("answer_generation", capturedCommand.getWorkerCapability());
        assertTrue(capturedCommand.isStream());
        assertFalse(capturedCommand.isCreateCandidate());
        assertEquals("STREAM_SUCCEEDED", result.getStatus());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION.value(), result.getCapability());
    }

    @Test
    void generateAnswerShouldUseAnswerGenerationWorkerAndReturnCallId() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);

        DiscoveryAiInvokeResult result = service.generateAnswer(command(false));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("DISCOVERY_ANSWER_GENERATION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION, capturedCommand.getCapability());
        assertFalse(capturedCommand.isStream());
        assertFalse(capturedCommand.isCreateCandidate());
        assertEquals(101L, result.getCallId());
        assertEquals(102L, result.getCandidateId());
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION.value(), result.getCapability());
    }

    @Test
    void generateAnswerShouldExposeFinalFailureStateFromInvocationResult() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        invocationService.failed = true;
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);

        DiscoveryAiInvokeResult result = service.generateAnswer(command(false));

        assertNotNull(result);
        assertEquals(101L, result.getCallId());
        assertEquals("FAILED", result.getStatus());
        assertEquals("WORKER_STREAM", result.getErrorType());
        assertEquals("stream interrupted", result.getErrorMessage());
    }

    @Test
    void understandQueryShouldResolveBusinessPromptWhenRequestOmitsModelAndPromptFields() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessResolver = new CapturingBusinessInvokeConfigResolver();
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, businessResolver);
        DiscoveryAiCommand command = command(false);
        command.setServiceId(null);
        command.setServiceRole(null);
        command.setModelId(null);
        command.setModelName(null);
        command.setPromptVersionId(null);
        command.setPromptMessagesJson(null);

        service.understandQuery(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(capturedCommand, businessResolver.capturedCommand());
        assertEquals(2001L, capturedCommand.getModelId());
        assertEquals("gpt-4o", capturedCommand.getModelName());
        assertEquals(940106L, capturedCommand.getPromptVersionId());
        assertEquals("[{\"role\":\"user\",\"content\":\"rendered\"}]", capturedCommand.getPromptMessagesJson());
    }

    private DiscoveryAiCommand command(boolean stream) {
        return new DiscoveryAiCommand(
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
            result.setCapability(command.getCapability().value());
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
            eventConsumer.accept(delta("王圻"));
            eventConsumer.accept(delta("文档答案"));
            AiInvokeResult result = new AiInvokeResult();
            result.setCallId(201L);
            result.setCandidateId(null);
            result.setRequestId(command.getRequestId());
            result.setTraceId(command.getTraceId());
            result.setStatus("STREAM_SUCCEEDED");
            result.setCapability(command.getCapability().value());
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

        private AiStreamEventResult delta(String content) {
            AiStreamEventResult event = new AiStreamEventResult();
            event.setEventType("delta");
            event.setRequestId("req-1");
            event.setTraceId("trace-1");
            event.setDeltaText(content);
            return event;
        }
    }

    private static class CapturingBusinessInvokeConfigResolver extends AiBusinessInvokeConfigResolver {

        private AiInvokeCommand captured;

        CapturingBusinessInvokeConfigResolver() {
            super(null, null, null, null);
        }

        @Override
        public void resolve(AiInvokeCommand command) {
            captured = command;
            command.setServiceId(1001L);
            command.setServiceRole("PRIMARY");
            command.setModelId(2001L);
            command.setModelName("gpt-4o");
            command.setPromptVersionId(940106L);
            command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"rendered\"}]");
            command.setPromptVariablesJson("{\"query\":\"hello\"}");
        }

        private AiInvokeCommand capturedCommand() {
            return captured;
        }
    }
}
