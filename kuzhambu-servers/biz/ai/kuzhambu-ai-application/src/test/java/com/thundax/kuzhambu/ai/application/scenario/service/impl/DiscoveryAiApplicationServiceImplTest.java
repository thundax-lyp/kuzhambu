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
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
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
        assertEquals("discovery", capturedCommand.scope());
        assertEquals("DISCOVERY_QUERY_UNDERSTANDING", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.DISCOVERY_QUERY_UNDERSTANDING, capturedCommand.capability());
        assertEquals("query_understanding", capturedCommand.workerCapability());
        assertEquals("DISCOVERY_QUERY", capturedCommand.contentRef().contentType());
        assertFalse(capturedCommand.stream());
        assertFalse(capturedCommand.createCandidate());
        assertEquals(AiInvocationStatus.SUCCEEDED, result.getStatus());
        assertEquals(AiBusinessCapability.DISCOVERY_QUERY_UNDERSTANDING, result.getCapability());
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
        assertEquals("DISCOVERY_ANSWER_GENERATION_STREAM", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION, capturedCommand.capability());
        assertEquals("answer_generation", capturedCommand.workerCapability());
        assertTrue(capturedCommand.stream());
        assertFalse(capturedCommand.createCandidate());
        assertEquals(AiInvocationStatus.SUCCEEDED, result.getStatus());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION, result.getCapability());
    }

    @Test
    void generateAnswerShouldUseAnswerGenerationWorkerAndReturnCallId() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);

        DiscoveryAiInvokeResult result = service.generateAnswer(command(false));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("DISCOVERY_ANSWER_GENERATION", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION, capturedCommand.capability());
        assertFalse(capturedCommand.stream());
        assertFalse(capturedCommand.createCandidate());
        assertEquals(new AiCallId(101L), result.getCallId());
        assertEquals(new AiCandidateId(102L), result.getCandidateId());
        assertEquals(AiInvocationStatus.SUCCEEDED, result.getStatus());
        assertEquals(AiBusinessCapability.DISCOVERY_ANSWER_GENERATION, result.getCapability());
    }

    @Test
    void generateAnswerShouldExposeFinalFailureStateFromInvocationResult() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        invocationService.failed = true;
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, null);

        DiscoveryAiInvokeResult result = service.generateAnswer(command(false));

        assertNotNull(result);
        assertEquals(new AiCallId(101L), result.getCallId());
        assertEquals(AiInvocationStatus.FAILED, result.getStatus());
        assertEquals("WORKER_STREAM", result.getErrorType());
        assertEquals("stream interrupted", result.getErrorMessage());
    }

    @Test
    void understandQueryShouldResolveBusinessPromptWhenRequestOmitsModelAndPromptFields() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessResolver = new CapturingBusinessInvokeConfigResolver();
        DiscoveryAiApplicationServiceImpl service =
                new DiscoveryAiApplicationServiceImpl(invocationService, resolver, businessResolver);
        DiscoveryAiCommand command = new DiscoveryAiCommand(
                null,
                null,
                null,
                null,
                null,
                new RequestId("req-1"),
                new TraceId("trace-1"),
                null,
                "{\"locale\":\"zh-CN\"}",
                "hash-a",
                "{\"query\":\"hello\"}",
                "{\"type\":\"object\"}",
                false,
                true,
                "zh-CN");

        service.understandQuery(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("discovery", businessResolver.capturedCommand().scope());
        assertEquals(
                AiBusinessCapability.DISCOVERY_QUERY_UNDERSTANDING,
                businessResolver.capturedCommand().capability());
        assertEquals(2001L, capturedCommand.modelId().value());
        assertEquals("gpt-4o", capturedCommand.modelName().value());
        assertEquals(6L, capturedCommand.promptVersionId().value());
        assertEquals("[{\"role\":\"user\",\"content\":\"rendered\"}]", capturedCommand.promptMessagesJson());
    }

    private DiscoveryAiCommand command(boolean stream) {
        return new DiscoveryAiCommand(
                3L,
                "discovery-portal",
                new AiModelId(10L),
                AiModelName.of("model-a"),
                new PromptVersionId(20L),
                new RequestId("req-1"),
                new TraceId("trace-1"),
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
            result.setCallId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId(101L));
            result.setCandidateId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId(102L));
            result.setRequestId(command.requestId());
            result.setTraceId(command.traceId());
            result.setStatus(
                    failed
                            ? com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.FAILED
                            : com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.SUCCEEDED);
            result.setCapability(command.capability());
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
            result.setCallId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId(201L));
            result.setCandidateId(null);
            result.setRequestId(command.requestId());
            result.setTraceId(command.traceId());
            result.setStatus(com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.SUCCEEDED);
            result.setCapability(command.capability());
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
            event.setRequestId(new com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId("req-1"));
            event.setTraceId(new com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId("trace-1"));
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
        public ResolvedBusinessInvokeConfig resolveConfig(AiInvokeCommand command) {
            captured = command;
            return resolved("{\"query\":\"hello\"}");
        }

        private ResolvedBusinessInvokeConfig resolved(String promptVariablesJson) {
            return new ResolvedBusinessInvokeConfig(
                    1001L,
                    "PRIMARY",
                    new com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId(2001L),
                    com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName.of("gpt-4o"),
                    new com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId(6L),
                    "[{\"role\":\"user\",\"content\":\"rendered\"}]",
                    promptVariablesJson,
                    "{\"type\":\"object\"}");
        }

        private AiInvokeCommand capturedCommand() {
            return captured;
        }
    }
}
