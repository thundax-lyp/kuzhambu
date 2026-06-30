package com.thundax.kuzhambu.ai.application.refinement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.refinement.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.refinement.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.refinement.support.ClassicsAiWorkerUsecaseResolver;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class AiRefinementApplicationServiceImplTest {

    private static final String CAPABILITY_TRANSLATE = "translate";
    private static final String CAPABILITY_SUMMARY = "summary";
    private static final String CAPABILITY_TAGS = "tags";
    private static final String CAPABILITY_QA = "qa";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "image_analysis";
    private static final String CAPABILITY_VISUAL = "visual";
    private static final String CAPABILITY_SPLIT = "split";

    private final ClassicsAiWorkerUsecaseResolver resolver = new ClassicsAiWorkerUsecaseResolver();

    @Test
    void summarizeShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result = service.summarize(command("SANCAI_ENTRY", "external-operation", CAPABILITY_SUMMARY));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_SUMMARY", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/sancai/summary", capturedCommand.getWorkerPath());
        assertEquals("summary", capturedCommand.getCapability());
    }

    @Test
    void summarizeShouldUseClassicsUsecasePathForWangqi() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result = service.summarize(command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_SUMMARY));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_WANGQI_SUMMARY", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/wangqi/summary", capturedCommand.getWorkerPath());
        assertEquals("summary", capturedCommand.getCapability());
    }

    @Test
    void summarizeShouldUseClassicsUsecasePathForMingCustoms() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result = service.summarize(command("MING_CUSTOMS", "external-operation", CAPABILITY_SUMMARY));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_MING_CUSTOMS_SUMMARY", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/ming-customs/summary", capturedCommand.getWorkerPath());
        assertEquals("summary", capturedCommand.getCapability());
    }

    @Test
    void generateTagsShouldUseClassicsUsecasePathForWangqi() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result =
                service.generateTags(command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_TAGS));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_WANGQI_TAGS", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/wangqi/tags", capturedCommand.getWorkerPath());
        assertEquals("tags", capturedCommand.getCapability());
    }

    @Test
    void generateQaShouldUseClassicsUsecasePathForMingCustoms() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result = service.generateQa(command("MING_CUSTOMS", "external-operation", CAPABILITY_QA));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_MING_CUSTOMS_QA", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/ming-customs/qa", capturedCommand.getWorkerPath());
        assertEquals("qa", capturedCommand.getCapability());
    }

    @Test
    void translateShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result =
                service.translate(command("SANCAI_ENTRY", "external-operation", CAPABILITY_TRANSLATE));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_TRANSLATE", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/sancai/translate", capturedCommand.getWorkerPath());
        assertEquals("translate", capturedCommand.getCapability());
    }

    @Test
    void describeVisualShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result =
                service.describeVisual(command("SANCAI_ENTRY", "external-operation", CAPABILITY_VISUAL));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_VISUAL_DESCRIPTION", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/sancai/visual-description", capturedCommand.getWorkerPath());
        assertEquals("visual", capturedCommand.getCapability());
    }

    @Test
    void splitEntryShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result = service.splitEntry(command("SANCAI_ENTRY", "external-operation", CAPABILITY_SPLIT));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_SPLIT", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/sancai/split", capturedCommand.getWorkerPath());
        assertEquals("split", capturedCommand.getCapability());
    }

    @Test
    void analyzeImageShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver);

        AiCandidateResult result =
                service.analyzeImage(command("SANCAI_ENTRY", "external-operation", CAPABILITY_IMAGE_ANALYSIS));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("MARKDOWN", result.getResultFormat());
        assertEquals("image-analysis-body", result.getResultPayload());
        assertEquals("CLASSICS_SANCAI_IMAGE_ANALYSIS", capturedCommand.getOperation());
        assertEquals("/internal/ai/classics/sancai/image-analysis", capturedCommand.getWorkerPath());
        assertEquals("image_analysis", capturedCommand.getCapability());
        assertEquals(true, capturedCommand.isStream());
        assertEquals(true, capturedCommand.isCreateCandidate());
        assertEquals(true, capturedCommand.toRunningCallRecord().isStreamUsed());
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
            result.setRequestId("req-1");
            result.setTraceId("trace-1");
            result.setStatus("SUCCEEDED");
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
