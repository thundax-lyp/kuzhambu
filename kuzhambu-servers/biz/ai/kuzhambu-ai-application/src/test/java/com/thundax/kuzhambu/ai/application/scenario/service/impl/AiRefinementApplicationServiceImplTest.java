package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.AiRefinementRequestCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.AiCandidateResult;
import com.thundax.kuzhambu.ai.application.scenario.support.ClassicsAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class AiRefinementApplicationServiceImplTest {

    private static final String CAPABILITY_TRANSLATE = "CLASSICS_TRANSLATE";
    private static final String CAPABILITY_SUMMARY = "CLASSICS_SUMMARY";
    private static final String CAPABILITY_TAGS = "CLASSICS_TAG_EXTRACT";
    private static final String CAPABILITY_QA = "CLASSICS_QA";
    private static final String CAPABILITY_IMAGE_ANALYSIS = "CLASSICS_IMAGE_DESCRIBE";
    private static final String CAPABILITY_IMAGE_GEN = "CLASSICS_IMAGE_GENERATE";
    private static final String CAPABILITY_VISUAL = "CLASSICS_VISUAL_DESCRIBE";
    private static final String CAPABILITY_SPLIT = "CLASSICS_SPLIT";

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
        assertEquals(AiBusinessCapability.from(CAPABILITY_SUMMARY), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_SUMMARY), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_SUMMARY), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_TAGS), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_QA), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_TRANSLATE), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_VISUAL), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_SPLIT), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_IMAGE_ANALYSIS), capturedCommand.getCapability());
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
        assertEquals(AiBusinessCapability.from(CAPABILITY_IMAGE_GEN), capturedCommand.getCapability());
        assertEquals(true, capturedCommand.isStream());
        assertEquals(true, invocationService.streamInvoked());
    }

    @Test
    void refinementShouldResolvePromptAndModelFromServerConfigWhenSnapshotIsMissing() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessConfigResolver = new CapturingBusinessInvokeConfigResolver();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, businessConfigResolver);
        AiRefinementRequestCommand command = command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_QA);
        command.setModelId(null);
        command.setModelName(null);
        command.setPromptVersionId(null);
        command.setPromptMessagesJson(null);

        service.generateQa(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(1, businessConfigResolver.resolveCount());
        assertEquals(new AiModelId(200L), capturedCommand.getModelId());
        assertEquals(AiModelName.of("server-model"), capturedCommand.getModelName());
        assertEquals(new PromptVersionId(300L), capturedCommand.getPromptVersionId());
        assertEquals("[{\"role\":\"system\",\"content\":\"server prompt\"}]", capturedCommand.getPromptMessagesJson());
        assertEquals("{\"title\":\"server variables\"}", capturedCommand.getPromptVariablesJson());
        assertEquals("{\"type\":\"object\"}", capturedCommand.getOutputSchemaJson());
    }

    @Test
    void refinementShouldResolvePromptAndModelFromServerConfigWhenSnapshotIsIncomplete() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessConfigResolver = new CapturingBusinessInvokeConfigResolver();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, businessConfigResolver);
        AiRefinementRequestCommand command = command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_QA);
        command.setPromptVariablesJson(null);
        command.setOutputSchemaJson(null);

        service.generateQa(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(1, businessConfigResolver.resolveCount());
        assertEquals(new AiModelId(200L), capturedCommand.getModelId());
        assertEquals(AiModelName.of("server-model"), capturedCommand.getModelName());
        assertEquals(new PromptVersionId(300L), capturedCommand.getPromptVersionId());
        assertEquals("[{\"role\":\"system\",\"content\":\"server prompt\"}]", capturedCommand.getPromptMessagesJson());
        assertEquals("{\"title\":\"server variables\"}", capturedCommand.getPromptVariablesJson());
        assertEquals("{\"type\":\"object\"}", capturedCommand.getOutputSchemaJson());
    }

    @Test
    void refinementShouldPreserveSubmittedPromptAndModelSnapshot() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessConfigResolver = new CapturingBusinessInvokeConfigResolver();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, businessConfigResolver);
        AiRefinementRequestCommand command = command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_QA);
        command.setPromptVariablesJson("{\"title\":\"submitted variables\"}");
        command.setOutputSchemaJson("{\"type\":\"object\",\"required\":[\"qaPairs\"]}");

        service.generateQa(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(0, businessConfigResolver.resolveCount());
        assertEquals(new AiModelId(20L), capturedCommand.getModelId());
        assertEquals(AiModelName.of("model-a"), capturedCommand.getModelName());
        assertEquals(new PromptVersionId(30L), capturedCommand.getPromptVersionId());
        assertEquals("[{\"role\":\"user\",\"content\":\"hello\"}]", capturedCommand.getPromptMessagesJson());
        assertEquals("{\"title\":\"submitted variables\"}", capturedCommand.getPromptVariablesJson());
        assertEquals("{\"type\":\"object\",\"required\":[\"qaPairs\"]}", capturedCommand.getOutputSchemaJson());
    }

    @Test
    void refinementShouldPreserveSubmittedSnapshotWithoutOutputSchema() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessConfigResolver = new CapturingBusinessInvokeConfigResolver();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, businessConfigResolver);
        AiRefinementRequestCommand command = command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_SUMMARY);
        command.setPromptVariablesJson("{\"title\":\"submitted variables\"}");
        command.setOutputSchemaJson(null);

        service.summarize(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(0, businessConfigResolver.resolveCount());
        assertEquals(new AiModelId(20L), capturedCommand.getModelId());
        assertEquals(new PromptVersionId(30L), capturedCommand.getPromptVersionId());
        assertEquals("[{\"role\":\"user\",\"content\":\"hello\"}]", capturedCommand.getPromptMessagesJson());
        assertEquals("{\"title\":\"submitted variables\"}", capturedCommand.getPromptVariablesJson());
        assertNull(capturedCommand.getOutputSchemaJson());
    }

    private AiRefinementRequestCommand command(String contentType, String operation, String capability) {
        AiRefinementRequestCommand command = new AiRefinementRequestCommand();
        command.setScope("classics");
        command.setOperation(operation);
        command.setContentRef(AiContentRef.ofNullable(contentType, 10L));
        command.setModelId(new AiModelId(20L));
        command.setModelName(AiModelName.of("model-a"));
        command.setPromptVersionId(new PromptVersionId(30L));
        command.setRequestId(new RequestId("req-1"));
        command.setTraceId(new TraceId("trace-1"));
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

    private static class CapturingBusinessInvokeConfigResolver extends AiBusinessInvokeConfigResolver {

        private int resolveCount;

        CapturingBusinessInvokeConfigResolver() {
            super(null, null, null, null);
        }

        @Override
        public void resolve(AiInvokeCommand command) {
            resolveCount++;
            command.setModelId(new AiModelId(200L));
            command.setModelName(AiModelName.of("server-model"));
            command.setPromptVersionId(new PromptVersionId(300L));
            command.setServiceRole("SERVER");
            command.setPromptMessagesJson("[{\"role\":\"system\",\"content\":\"server prompt\"}]");
            command.setPromptVariablesJson("{\"title\":\"server variables\"}");
            command.setOutputSchemaJson("{\"type\":\"object\"}");
        }

        int resolveCount() {
            return resolveCount;
        }
    }
}
