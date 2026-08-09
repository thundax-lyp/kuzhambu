package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.scenario.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.scenario.result.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.application.scenario.support.KnowledgeAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class KnowledgeAiExtractionApplicationServiceImplTest {

    private final KnowledgeAiWorkerUsecaseResolver resolver = new KnowledgeAiWorkerUsecaseResolver();

    @Test
    void extractGraphShouldUseKnowledgeGraphUsecase() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        KnowledgeAiExtractionResult result = extractGraph(repository);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("KNOWLEDGE_GRAPH_EXTRACTION", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT, capturedCommand.capability());
        assertEquals("knowledge_graph", capturedCommand.workerCapability());
        assertEquals(0, invocationService.invokeCount());
        assertEquals(1, invocationService.streamCount());
    }

    @Test
    void extractRelationShouldUseRelationExtractionCapability() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        extractRelations(repository);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("KNOWLEDGE_RELATION_EXTRACTION", capturedCommand.operation());
        assertEquals(AiBusinessCapability.KNOWLEDGE_RELATION_EXTRACT, capturedCommand.capability());
    }

    @Test
    void extractLineageShouldUseLineageUsecase() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        extractLineage(repository);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("KNOWLEDGE_LINEAGE_EXTRACTION", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_LINEAGE_EXTRACT, capturedCommand.capability());
    }

    @Test
    void extractTagsShouldUseTagExtractionUsecaseAndCreateCandidate() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        extractTags(repository);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("knowledge", capturedCommand.scope());
        assertEquals("KNOWLEDGE_TAG_EXTRACTION", capturedCommand.operation());
        assertNull(capturedCommand.workerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_TAG_EXTRACT, capturedCommand.capability());
        assertEquals("tags", capturedCommand.workerCapability());
        assertEquals(true, capturedCommand.forceJson());
        assertEquals(true, capturedCommand.createCandidate());
    }

    @Test
    void extractGraphShouldPreserveCallAndCandidateIdentifiers() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        KnowledgeAiExtractionResult result = extractGraph(repository);

        assertEquals(new AiCallId(101L), result.getCallId());
        assertEquals(new AiCandidateId(102L), result.getCandidateId());
        assertEquals(AiInvocationStatus.SUCCEEDED, result.getStatus());
        assertEquals(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT, result.getCapability());
    }

    @Test
    void extractGraphShouldResolveBusinessPromptWhenRequestOmitsModelAndPromptFields() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessResolver = new CapturingBusinessInvokeConfigResolver();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, businessResolver);
        KnowledgeAiExtractionCommand command = new KnowledgeAiExtractionCommand(
                "GRAPH",
                "ENTRY",
                "{\"entryIds\":[1]}",
                "SANCAI_ENTRY",
                1L,
                2L,
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
                "{\"text\":\"hello\"}",
                "{\"type\":\"object\"}",
                true,
                "zh-CN");

        repository.extractGraph(command);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("knowledge", businessResolver.capturedCommand().scope());
        assertEquals(
                AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT,
                businessResolver.capturedCommand().capability());
        assertEquals(2001L, capturedCommand.modelId().value());
        assertEquals("gpt-4o", capturedCommand.modelName().value());
        assertEquals(6L, capturedCommand.promptVersionId().value());
        assertEquals("[{\"role\":\"user\",\"content\":\"rendered\"}]", capturedCommand.promptMessagesJson());
    }

    private KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionApplicationServiceImpl repository) {
        return repository.extractGraph(command());
    }

    private KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionApplicationServiceImpl repository) {
        return repository.extractRelations(command());
    }

    private KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionApplicationServiceImpl repository) {
        return repository.extractLineage(command());
    }

    private KnowledgeAiExtractionResult extractTags(KnowledgeAiExtractionApplicationServiceImpl repository) {
        return repository.extractTags(command());
    }

    private KnowledgeAiExtractionCommand command() {
        return new KnowledgeAiExtractionCommand(
                "GRAPH",
                "ENTRY",
                "{\"entryIds\":[1]}",
                "SANCAI_ENTRY",
                1L,
                2L,
                3L,
                "knowledge-admin",
                new AiModelId(10L),
                AiModelName.of("model-a"),
                new PromptVersionId(20L),
                new RequestId("req-1"),
                new TraceId("trace-1"),
                "[{\"role\":\"user\",\"content\":\"hello\"}]",
                null,
                null,
                "{\"text\":\"hello\"}",
                "{\"type\":\"object\"}",
                true,
                "zh-CN");
    }

    private static class CapturingInvocationService implements AiWorkerInvocationApplicationService {

        private AiInvokeCommand captured;
        private int invokeCount;
        private int streamCount;

        @Override
        public AiInvokeResult invoke(AiInvokeCommand command) {
            invokeCount++;
            captured = command;
            return succeeded(command);
        }

        @Override
        public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
            streamCount++;
            captured = command;
            return succeeded(command);
        }

        private AiInvokeResult succeeded(AiInvokeCommand command) {
            AiInvokeResult result = new AiInvokeResult();
            result.setCallId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCallId(101L));
            result.setCandidateId(new com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiCandidateId(102L));
            result.setRequestId(command.requestId());
            result.setTraceId(command.traceId());
            result.setStatus(com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus.SUCCEEDED);
            result.setCapability(command.capability());
            result.setResultFormat("STRUCTURED");
            result.setResultPayload("{\"nodes\":[]}");
            return result;
        }

        public AiInvokeCommand capturedCommand() {
            return captured;
        }

        public int invokeCount() {
            return invokeCount;
        }

        public int streamCount() {
            return streamCount;
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
            return new ResolvedBusinessInvokeConfig(
                    1001L,
                    "PRIMARY",
                    new com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId(2001L),
                    com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName.of("gpt-4o"),
                    new com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId(6L),
                    "[{\"role\":\"user\",\"content\":\"rendered\"}]",
                    "{\"text\":\"hello\"}",
                    "{\"type\":\"object\"}");
        }

        private AiInvokeCommand capturedCommand() {
            return captured;
        }
    }
}
