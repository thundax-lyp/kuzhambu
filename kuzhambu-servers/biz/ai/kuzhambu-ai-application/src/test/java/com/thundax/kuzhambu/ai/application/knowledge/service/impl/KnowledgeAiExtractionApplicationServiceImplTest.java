package com.thundax.kuzhambu.ai.application.knowledge.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.support.AiBusinessInvokeConfigResolver;
import com.thundax.kuzhambu.ai.application.knowledge.command.KnowledgeAiExtractionCommand;
import com.thundax.kuzhambu.ai.application.knowledge.result.KnowledgeAiExtractionResult;
import com.thundax.kuzhambu.ai.application.knowledge.support.KnowledgeAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
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
        assertEquals("KNOWLEDGE_GRAPH_EXTRACTION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT.value(), capturedCommand.getCapability());
        assertEquals("knowledge_graph", capturedCommand.getWorkerCapability());
    }

    @Test
    void extractRelationShouldUseRelationExtractionCapability() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        extractRelations(repository);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("KNOWLEDGE_RELATION_EXTRACTION", capturedCommand.getOperation());
        assertEquals(AiBusinessCapability.KNOWLEDGE_RELATION_EXTRACT.value(), capturedCommand.getCapability());
    }

    @Test
    void extractLineageShouldUseLineageUsecase() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        extractLineage(repository);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("KNOWLEDGE_LINEAGE_EXTRACTION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_LINEAGE_EXTRACT.value(), capturedCommand.getCapability());
    }

    @Test
    void extractTagsShouldUseTagExtractionUsecaseAndCreateCandidate() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        extractTags(repository);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("knowledge", capturedCommand.getScope());
        assertEquals("KNOWLEDGE_TAG_EXTRACTION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_TAG_EXTRACT.value(), capturedCommand.getCapability());
        assertEquals("tags", capturedCommand.getWorkerCapability());
        assertEquals(true, capturedCommand.isForceJson());
        assertEquals(true, capturedCommand.isCreateCandidate());
    }

    @Test
    void extractGraphShouldPreserveCallAndCandidateIdentifiers() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        KnowledgeAiExtractionResult result = extractGraph(repository);

        assertEquals(101L, result.getCallId());
        assertEquals(102L, result.getCandidateId());
        assertEquals("SUCCEEDED", result.getStatus());
    }

    @Test
    void extractGraphShouldResolveBusinessPromptWhenRequestOmitsModelAndPromptFields() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        CapturingBusinessInvokeConfigResolver businessResolver = new CapturingBusinessInvokeConfigResolver();
        KnowledgeAiExtractionApplicationServiceImpl repository =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, businessResolver);
        KnowledgeAiExtractionCommand input = input();
        input.setServiceId(null);
        input.setServiceRole(null);
        input.setModelId(null);
        input.setModelName(null);
        input.setPromptVersionId(null);
        input.setPromptMessagesJson(null);

        repository.extractGraph(input);
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals(capturedCommand, businessResolver.capturedCommand());
        assertEquals(2001L, capturedCommand.getModelId());
        assertEquals("gpt-4o", capturedCommand.getModelName());
        assertEquals(940106L, capturedCommand.getPromptVersionId());
        assertEquals("[{\"role\":\"user\",\"content\":\"rendered\"}]", capturedCommand.getPromptMessagesJson());
    }

    private KnowledgeAiExtractionResult extractGraph(KnowledgeAiExtractionApplicationServiceImpl repository) {
        return repository.extractGraph(input());
    }

    private KnowledgeAiExtractionResult extractRelations(KnowledgeAiExtractionApplicationServiceImpl repository) {
        return repository.extractRelations(input());
    }

    private KnowledgeAiExtractionResult extractLineage(KnowledgeAiExtractionApplicationServiceImpl repository) {
        return repository.extractLineage(input());
    }

    private KnowledgeAiExtractionResult extractTags(KnowledgeAiExtractionApplicationServiceImpl repository) {
        return repository.extractTags(input());
    }

    private KnowledgeAiExtractionCommand input() {
        return new KnowledgeAiExtractionCommand(
                "GRAPH",
                "ENTRY",
                "{\"entryIds\":[1]}",
                "SANCAI_ENTRY",
                1L,
                2L,
                3L,
                "knowledge-admin",
                10L,
                "model-a",
                20L,
                "req-1",
                "trace-1",
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

        @Override
        public AiInvokeResult invoke(AiInvokeCommand command) {
            captured = command;
            AiInvokeResult result = new AiInvokeResult();
            result.setCallId(101L);
            result.setCandidateId(102L);
            result.setRequestId(command.getRequestId());
            result.setTraceId(command.getTraceId());
            result.setStatus("SUCCEEDED");
            result.setCapability(command.getCapability());
            result.setResultFormat("STRUCTURED");
            result.setResultPayload("{\"nodes\":[]}");
            return result;
        }

        @Override
        public AiInvokeResult stream(AiInvokeCommand command, Consumer<AiStreamEventResult> eventConsumer) {
            return null;
        }

        public AiInvokeCommand capturedCommand() {
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
            command.setModelId(2001L);
            command.setModelName("gpt-4o");
            command.setPromptVersionId(940106L);
            command.setPromptMessagesJson("[{\"role\":\"user\",\"content\":\"rendered\"}]");
            command.setPromptVariablesJson("{\"text\":\"hello\"}");
        }

        private AiInvokeCommand capturedCommand() {
            return captured;
        }
    }
}
