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
        assertEquals("CLASSICS_SANCAI_SUMMARY", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_SUMMARY), capturedCommand.capability());
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
        assertEquals("CLASSICS_WANGQI_SUMMARY", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_SUMMARY), capturedCommand.capability());
    }

    @Test
    void summarizeShouldUseClassicsUsecasePathForMingCustoms() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result = service.summarize(command("MING_CUSTOMS", "external-operation", CAPABILITY_SUMMARY));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_MING_CUSTOMS_SUMMARY", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_SUMMARY), capturedCommand.capability());
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
        assertEquals("CLASSICS_WANGQI_TAGS", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_TAGS), capturedCommand.capability());
    }

    @Test
    void generateQaShouldUseClassicsUsecasePathForMingCustoms() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result = service.generateQa(command("MING_CUSTOMS", "external-operation", CAPABILITY_QA));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_MING_CUSTOMS_QA", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_QA), capturedCommand.capability());
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
        assertEquals("CLASSICS_SANCAI_TRANSLATE", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_TRANSLATE), capturedCommand.capability());
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
        assertEquals("CLASSICS_SANCAI_VISUAL_DESCRIPTION", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_VISUAL), capturedCommand.capability());
    }

    @Test
    void splitEntryShouldUseClassicsUsecasePathForSancai() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, null);

        AiCandidateResult result = service.splitEntry(command("SANCAI_ENTRY", "external-operation", CAPABILITY_SPLIT));
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("CLASSICS_SANCAI_SPLIT", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_SPLIT), capturedCommand.capability());
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
        assertEquals("CLASSICS_SANCAI_IMAGE_ANALYSIS", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_IMAGE_ANALYSIS), capturedCommand.capability());
        assertEquals(true, capturedCommand.stream());
        assertEquals(true, capturedCommand.createCandidate());
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
        assertEquals("CLASSICS_SANCAI_IMAGE_GEN", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.from(CAPABILITY_IMAGE_GEN), capturedCommand.capability());
        assertEquals(true, capturedCommand.stream());
        assertEquals(true, invocationService.streamInvoked());
    }

    @Test
    void refinementShouldResolvePromptAndModelFromServerConfigWhenSnapshotIsMissing() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessConfigResolver = new CapturingBusinessInvokeConfigResolver();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, businessConfigResolver);
        AiRefinementRequestCommand command =
                command("WANGQI_DOCUMENT", "external-operation", CAPABILITY_QA, null, null, null, null, null, null);

        service.generateQa(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(1, businessConfigResolver.resolveCount());
        assertEquals(new AiModelId(200L), capturedCommand.modelId());
        assertEquals(AiModelName.of("server-model"), capturedCommand.modelName());
        assertEquals(new PromptVersionId(300L), capturedCommand.promptVersionId());
        assertEquals("[{\"role\":\"system\",\"content\":\"server prompt\"}]", capturedCommand.promptMessagesJson());
        assertEquals("{\"title\":\"server variables\"}", capturedCommand.promptVariablesJson());
        assertEquals("{\"type\":\"object\"}", capturedCommand.outputSchemaJson());
    }

    @Test
    void refinementShouldResolvePromptAndModelFromServerConfigWhenSnapshotIsIncomplete() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessConfigResolver = new CapturingBusinessInvokeConfigResolver();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, businessConfigResolver);
        AiRefinementRequestCommand command = command(
                "WANGQI_DOCUMENT",
                "external-operation",
                CAPABILITY_QA,
                new AiModelId(20L),
                AiModelName.of("model-a"),
                new PromptVersionId(30L),
                "[{\"role\":\"user\",\"content\":\"hello\"}]",
                null,
                null);

        service.generateQa(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(1, businessConfigResolver.resolveCount());
        assertEquals(new AiModelId(200L), capturedCommand.modelId());
        assertEquals(AiModelName.of("server-model"), capturedCommand.modelName());
        assertEquals(new PromptVersionId(300L), capturedCommand.promptVersionId());
        assertEquals("[{\"role\":\"system\",\"content\":\"server prompt\"}]", capturedCommand.promptMessagesJson());
        assertEquals("{\"title\":\"server variables\"}", capturedCommand.promptVariablesJson());
        assertEquals("{\"type\":\"object\"}", capturedCommand.outputSchemaJson());
    }

    @Test
    void refinementShouldPreserveSubmittedPromptAndModelSnapshot() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessConfigResolver = new CapturingBusinessInvokeConfigResolver();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, businessConfigResolver);
        AiRefinementRequestCommand command = command(
                "WANGQI_DOCUMENT",
                "external-operation",
                CAPABILITY_QA,
                new AiModelId(20L),
                AiModelName.of("model-a"),
                new PromptVersionId(30L),
                "[{\"role\":\"user\",\"content\":\"hello\"}]",
                "{\"title\":\"submitted variables\"}",
                "{\"type\":\"object\",\"required\":[\"qaPairs\"]}");

        service.generateQa(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(0, businessConfigResolver.resolveCount());
        assertEquals(new AiModelId(20L), capturedCommand.modelId());
        assertEquals(AiModelName.of("model-a"), capturedCommand.modelName());
        assertEquals(new PromptVersionId(30L), capturedCommand.promptVersionId());
        assertEquals("[{\"role\":\"user\",\"content\":\"hello\"}]", capturedCommand.promptMessagesJson());
        assertEquals("{\"title\":\"submitted variables\"}", capturedCommand.promptVariablesJson());
        assertEquals("{\"type\":\"object\",\"required\":[\"qaPairs\"]}", capturedCommand.outputSchemaJson());
    }

    @Test
    void refinementShouldPreserveSubmittedSnapshotWithoutOutputSchema() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessConfigResolver = new CapturingBusinessInvokeConfigResolver();
        AiRefinementApplicationServiceImpl service =
                new AiRefinementApplicationServiceImpl(invocationService, resolver, businessConfigResolver);
        AiRefinementRequestCommand command = command(
                "WANGQI_DOCUMENT",
                "external-operation",
                CAPABILITY_SUMMARY,
                new AiModelId(20L),
                AiModelName.of("model-a"),
                new PromptVersionId(30L),
                "[{\"role\":\"user\",\"content\":\"hello\"}]",
                "{\"title\":\"submitted variables\"}",
                null);

        service.summarize(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(0, businessConfigResolver.resolveCount());
        assertEquals(new AiModelId(20L), capturedCommand.modelId());
        assertEquals(new PromptVersionId(30L), capturedCommand.promptVersionId());
        assertEquals("[{\"role\":\"user\",\"content\":\"hello\"}]", capturedCommand.promptMessagesJson());
        assertEquals("{\"title\":\"submitted variables\"}", capturedCommand.promptVariablesJson());
        assertNull(capturedCommand.outputSchemaJson());
    }

    private AiRefinementRequestCommand command(String contentType, String operation, String capability) {
        return command(
                contentType,
                operation,
                capability,
                new AiModelId(20L),
                AiModelName.of("model-a"),
                new PromptVersionId(30L),
                "[{\"role\":\"user\",\"content\":\"hello\"}]",
                null,
                null);
    }

    private AiRefinementRequestCommand command(
            String contentType,
            String operation,
            String capability,
            AiModelId modelId,
            AiModelName modelName,
            PromptVersionId promptVersionId,
            String promptMessagesJson,
            String promptVariablesJson,
            String outputSchemaJson) {
        return new AiRefinementRequestCommand(
                null,
                AiBusinessCapability.from(capability),
                "classics",
                operation,
                AiContentRef.ofNullable(contentType, 10L),
                null,
                null,
                null,
                modelId,
                modelName,
                promptVersionId,
                new RequestId("req-1"),
                new TraceId("trace-1"),
                promptMessagesJson,
                promptVariablesJson,
                null,
                "{\"text\":\"hello\"}",
                outputSchemaJson,
                false,
                null);
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
            result.setCapability(command.capability());
            result.setResultFormat("MARKDOWN");
            result.setResultPayload("image-analysis-body");
            result.setStreamCompleted(command.stream());
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
        public ResolvedBusinessInvokeConfig resolve(AiInvokeCommand command) {
            resolveCount++;
            return new ResolvedBusinessInvokeConfig(
                    null,
                    "SERVER",
                    new AiModelId(200L),
                    AiModelName.of("server-model"),
                    new PromptVersionId(300L),
                    "[{\"role\":\"system\",\"content\":\"server prompt\"}]",
                    "{\"title\":\"server variables\"}",
                    "{\"type\":\"object\"}");
        }

        int resolveCount() {
            return resolveCount;
        }
    }
}
