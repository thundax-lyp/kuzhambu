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
        assertEquals("platform", capturedCommand.getScope());
        assertEquals("PLATFORM_PROMPT_SUGGESTION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.PROMPT_SUGGEST, capturedCommand.getCapability());
        assertEquals("prompt_suggestion", capturedCommand.getWorkerCapability());
        assertFalse(capturedCommand.isStream());
        assertTrue(capturedCommand.isCreateCandidate());
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
        assertEquals("PLATFORM_VERSION_SUMMARY", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.PLATFORM_VERSION_SUMMARY, capturedCommand.getCapability());
        assertEquals("version_summary", capturedCommand.getWorkerCapability());
        assertFalse(capturedCommand.isStream());
        assertFalse(capturedCommand.isCreateCandidate());
        assertEquals(AiBusinessCapability.PLATFORM_VERSION_SUMMARY, result.getCapability());
    }

    @Test
    void requestCanOverrideCandidateCreation() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        PlatformAiApplicationServiceImpl service =
                new PlatformAiApplicationServiceImpl(invocationService, resolver, null);
        PlatformAiInvokeCommand command = command();
        command.setCreateCandidate(Boolean.FALSE);

        service.buildPromptSuggestion(command);

        assertFalse(invocationService.capturedCommand().isCreateCandidate());
    }

    @Test
    void promptSuggestionShouldResolveBusinessPromptWhenCommandOmitsModelAndPromptFields() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessResolver = new CapturingBusinessInvokeConfigResolver();
        PlatformAiApplicationServiceImpl service =
                new PlatformAiApplicationServiceImpl(invocationService, resolver, businessResolver);
        PlatformAiInvokeCommand command = command();
        command.setServiceId(null);
        command.setServiceRole(null);
        command.setModelId(null);
        command.setModelName(null);
        command.setPromptVersionId(null);
        command.setPromptMessagesJson(null);

        service.buildPromptSuggestion(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(capturedCommand, businessResolver.capturedCommand());
        assertEquals(2001L, capturedCommand.getModelId().value());
        assertEquals("gpt-4o", capturedCommand.getModelName().value());
        assertEquals(6L, capturedCommand.getPromptVersionId().value());
        assertEquals("[{\"role\":\"user\",\"content\":\"rendered\"}]", capturedCommand.getPromptMessagesJson());
    }

    private PlatformAiInvokeCommand command() {
        PlatformAiInvokeCommand command = new PlatformAiInvokeCommand();
        command.setContentRef(AiContentRef.ofNullable("PROMPT_TEMPLATE", 10L));
        command.setTargetObjectId(new AiTargetObjectId(20L));
        command.setModelId(new AiModelId(30L));
        command.setModelName(AiModelName.of("model-a"));
        command.setPromptVersionId(new PromptVersionId(40L));
        command.setRequestId(new RequestId("req-1"));
        command.setTraceId(new TraceId("trace-1"));
        command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"hello\"}]");
        command.setInputPayloadJson("{\"template\":\"hello\"}");
        command.setOutputSchemaJson("{\"type\":\"object\"}");
        command.setForceJson(true);
        command.setLocale("zh-CN");
        return command;
    }

    private static class CapturingInvocationService implements AiWorkerInvocationApplicationService {

        private AiInvokeCommand captured;

        @Override
        public AiInvokeResult invoke(AiInvokeCommand command) {
            captured = command;
            AiInvokeResult result = new AiInvokeResult();
            result.setCallId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId(101L));
            result.setCandidateId(
                    command.isCreateCandidate()
                            ? new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId(102L)
                            : null);
            result.setRequestId(command.getRequestId());
            result.setTraceId(command.getTraceId());
            result.setStatus(com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.SUCCEEDED);
            result.setCapability(command.getCapability());
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
        public void resolve(AiInvokeCommand command) {
            captured = command;
            command.setServiceId(1001L);
            command.setServiceRole("PRIMARY");
            command.setModelId(new com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId(2001L));
            command.setModelName(com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName.of("gpt-4o"));
            command.setPromptVersionId(new com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId(6L));
            command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"rendered\"}]");
            command.setPromptVariablesJson("{\"template\":\"hello\"}");
        }

        private AiInvokeCommand capturedCommand() {
            return captured;
        }
    }
}
