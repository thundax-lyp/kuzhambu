package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.scenario.support.ClassicsAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class AiRefinementApplicationServiceImplTest {

    private static final String CAPABILITY_TRANSLATE = "classics_translate";
    private static final String CAPABILITY_SUMMARY = "classics_summary";
    private static final String CAPABILITY_TAGS = "classics_tags";
    private static final String CAPABILITY_QA = "classics_qa";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "classics_image_describe";
    private static final String CAPABILITY_IMAGE_GEN = "classics_image_generate";
    private static final String CAPABILITY_VISUAL = "classics_visual_describe";
    private static final String CAPABILITY_SPLIT = "classics_split";

    private final ClassicsAiWorkerUsecaseResolver resolver = new ClassicsAiWorkerUsecaseResolver();

    @Test
    void summarizeShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result = service.summarize(command("SANCAI_ENTRY", "external-operation", CAPABILITY_SUMMARY));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_SUMMARY", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_SUMMARY), capturedCommand.getCapability());
    }

    @Test
    void summarizeShouldUseClassicsUsecasePathForWangqi() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result =
                service.summarize(command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_SUMMARY));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_WANGQI_SUMMARY", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_SUMMARY), capturedCommand.getCapability());
    }

    @Test
    void summarizeShouldUseClassicsUsecasePathForMingCustoms() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result = service.summarize(command("MING_CUSTOMS", "external-operation", CAPABILITY_SUMMARY));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_MING_CUSTOMS_SUMMARY", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_SUMMARY), capturedCommand.getCapability());
    }

    @Test
    void generateTagsShouldUseClassicsUsecasePathForWangqi() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result =
                service.generateTags(command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_TAGS));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_WANGQI_TAGS", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_TAGS), capturedCommand.getCapability());
    }

    @Test
    void generateQaShouldUseClassicsUsecasePathForMingCustoms() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result = service.generateQa(command("MING_CUSTOMS", "external-operation", CAPABILITY_QA));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_MING_CUSTOMS_QA", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_QA), capturedCommand.getCapability());
    }

    @Test
    void translateShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result =
                service.translate(command("SANCAI_ENTRY", "external-operation", CAPABILITY_TRANSLATE));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_TRANSLATE", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_TRANSLATE), capturedCommand.getCapability());
    }

    @Test
    void describeVisualShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result =
                service.describeVisual(command("SANCAI_ENTRY", "external-operation", CAPABILITY_VISUAL));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_VISUAL_DESCRIPTION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_VISUAL), capturedCommand.getCapability());
    }

    @Test
    void splitEntryShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result = service.splitEntry(command("SANCAI_ENTRY", "external-operation", CAPABILITY_SPLIT));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_SPLIT", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_SPLIT), capturedCommand.getCapability());
    }

    @Test
    void analyzeImageShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result =
                service.analyzeImage(command("SANCAI_ENTRY", "external-operation", CAPABILITY_IMAGE_ANALYSIS));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("MARKDOWN", result.getResultFormat());
        assertEquals("image-analysis-body", result.getResultPayload());
        assertEquals("CLASSICS_SANCAI_IMAGE_ANALYSIS", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_IMAGE_ANALYSIS), capturedCommand.getCapability());
        assertEquals(true, capturedCommand.isStream());
        assertEquals(true, capturedCommand.isCreateCandidate());
        assertEquals(true, invocationService.streamInvoked());
    }

    @Test
    void generateImageShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result =
                service.generateImage(command("SANCAI_ENTRY", "external-operation", CAPABILITY_IMAGE_GEN));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_IMAGE_GEN", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.fromAlias(CAPABILITY_IMAGE_GEN), capturedCommand.getCapability());
        assertEquals(true, capturedCommand.isStream());
        assertEquals(true, invocationService.streamInvoked());
    }

    private AiRefinementRequestCommand command(String contentType, String operation, String capability) {
        AiRefinementRequestCommand command = new AiRefinementRequestCommand();
        command.setScope("classics");
        command.setOperation(operation);
        command.setContentType(contentType);
        command.setContentId(10L);
        command.setModelId(20L);
        command.setModelName("model-a");
        command.setPromptVersionId(30L);
        command.setRequestId("req-1");
        command.setTraceId("trace-1");
        command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"hello\"}]");
        command.setInputPayloadJson("{\"text\":\"hello\"}");
        return command;
    }

    private static class CapturingInvocationService implements AiWorkerInvocationApplicationService {

        private AiInvokeCommand captured;
        private boolean streamInvoked;

        @Override
        public AiInvokeResult invoke(AiInvokeCommand command) {
            captured = command;
            AiInvokeResult result = new AiInvokeResult();
            result.setRequestId(new com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId("req-1"));
            result.setTraceId(new com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId("trace-1"));
            result.setStatus(com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.SUCCEEDED);
            result.setCapability(command.getCapability());
            result.setResultFormat("MARKDOWN");
            result.setResultPayload("image-analysis-body");
            result.setStreamCompleted(command.isStream());
            return result;
        }

        @Override
        public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
            streamInvoked = true;
            return invoke(command);
        }

        public AiInvokeCommand capturedCommand() {
            return captured;
        }

        public boolean streamInvoked() {
            return streamInvoked;
        }
    }
}
