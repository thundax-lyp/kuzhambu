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
import com.thundax.kuzhambu.ai.application.scenario.command.PlatformAiInvokeCommand;
import com.thundax.kuzhambu.ai.application.scenario.support.PlatformAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class PlatformAiApplicationServiceImplTest {

    private final PlatformAiWorkerUsecaseResolver resolver = new PlatformAiWorkerUsecaseResolver();

    @Test
    void promptSuggestionShouldMapToPlatformWorkerUsecase() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        PlatformAiApplicationServiceImpl service =
                new PlatformAiApplicationServiceImpl(invocationService, resolver, null);

        AiInvokeResult result = service.buildPromptSuggestion(command());
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("platform", capturedCommand.scope());
        assertEquals("PLATFORM_PROMPT_SUGGESTION", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.PROMPT_SUGGEST, capturedCommand.capability());
        assertEquals("prompt_suggestion", capturedCommand.workerCapability());
        assertFalse(capturedCommand.stream());
        assertTrue(capturedCommand.createCandidate());
        assertEquals(AiBusinessCapability.PROMPT_SUGGEST, result.getCapability());
    }

    @Test
    void versionSummaryShouldMapToPlatformWorkerUsecaseWithoutCandidateByDefault() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        PlatformAiApplicationServiceImpl service =
                new PlatformAiApplicationServiceImpl(invocationService, resolver, null);

        AiInvokeResult result = service.summarizeVersion(command());
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("PLATFORM_VERSION_SUMMARY", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.PLATFORM_VERSION_SUMMARY, capturedCommand.capability());
        assertEquals("version_summary", capturedCommand.workerCapability());
        assertFalse(capturedCommand.stream());
        assertFalse(capturedCommand.createCandidate());
        assertEquals(AiBusinessCapability.PLATFORM_VERSION_SUMMARY, result.getCapability());
    }

    @Test
    void requestCanOverrideCandidateCreation() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        PlatformAiApplicationServiceImpl service =
                new PlatformAiApplicationServiceImpl(invocationService, resolver, null);

        service.buildPromptSuggestion(command(Boolean.FALSE));

        assertFalse(invocationService.capturedCommand().createCandidate());
    }

    @Test
    void promptSuggestionShouldResolveBusinessPromptWhenCommandOmitsModelAndPromptFields() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessResolver = new CapturingBusinessInvokeConfigResolver();
        PlatformAiApplicationServiceImpl service =
                new PlatformAiApplicationServiceImpl(invocationService, resolver, businessResolver);
        PlatformAiInvokeCommand command = new PlatformAiInvokeCommand(
                AiContentRef.ofNullable("PROMPT_TEMPLATE", 10L),
                new AiTargetObjectId(20L),
                null,
                null,
                null,
                null,
                null,
                new RequestId("req-1"),
                new TraceId("trace-1"),
                null,
                null,
                null,
                "{\"template\":\"hello\"}",
                "{\"type\":\"object\"}",
                true,
                "zh-CN",
                false,
                null);

        service.buildPromptSuggestion(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("platform", businessResolver.capturedCommand().scope());
        assertEquals(
                AiBusinessCapability.PROMPT_SUGGEST,
                businessResolver.capturedCommand().capability());
        assertEquals(2001L, capturedCommand.modelId().value());
        assertEquals("gpt-4o", capturedCommand.modelName().value());
        assertEquals(6L, capturedCommand.promptVersionId().value());
        assertEquals("[{\"role\":\"user\",\"content\":\"rendered\"}]", capturedCommand.promptMessagesJson());
    }

    private PlatformAiInvokeCommand command() {
        return command(null);
    }

    private PlatformAiInvokeCommand command(Boolean createCandidate) {
        return new PlatformAiInvokeCommand(
                AiContentRef.ofNullable("PROMPT_TEMPLATE", 10L),
                new AiTargetObjectId(20L),
                null,
                null,
                new AiModelId(30L),
                AiModelName.of("model-a"),
                new PromptVersionId(40L),
                new RequestId("req-1"),
                new TraceId("trace-1"),
                "[{\"role\":\"user\",\"content\":\"hello\"}]",
                null,
                null,
                "{\"template\":\"hello\"}",
                "{\"type\":\"object\"}",
                true,
                "zh-CN",
                false,
                createCandidate);
    }

    private static class CapturingInvocationService implements AiWorkerInvocationApplicationService {

        private AiInvokeCommand captured;

        @Override
        public AiInvokeResult invoke(AiInvokeCommand command) {
            captured = command;
            AiInvokeResult result = new AiInvokeResult();
            result.setCallId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId(101L));
            result.setCandidateId(
                    command.createCandidate()
                            ? new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId(102L)
                            : null);
            result.setRequestId(command.requestId());
            result.setTraceId(command.traceId());
            result.setStatus(com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.SUCCEEDED);
            result.setCapability(command.capability());
            result.setResultFormat("TEXT");
            result.setResultPayload("ok");
            return result;
        }

        @Override
        public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
            throw new UnsupportedOperationException("platform ai should use sync invoke");
        }

        private AiInvokeCommand capturedCommand() {
            return captured;
        }
    }

    private static class CapturingBusinessInvokeConfigResolver extends AiBusinessInvokeConfigResolver {

        private AiInvokeCommand captured;

        CapturingBusinessInvokeConfigResolver() {
            super(null, null, null, null);
        }

        @Override
        public ResolvedBusinessInvokeConfig resolve(AiInvokeCommand command) {
            captured = command;
            return new ResolvedBusinessInvokeConfig(
                    1001L,
                    "PRIMARY",
                    new com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId(2001L),
                    com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName.of("gpt-4o"),
                    new com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId(6L),
                    "[{\"role\":\"user\",\"content\":\"rendered\"}]",
                    "{\"template\":\"hello\"}",
                    "{\"type\":\"object\"}");
        }

        private AiInvokeCommand capturedCommand() {
            return captured;
        }
    }
}
