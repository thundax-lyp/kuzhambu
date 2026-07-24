package com.thundax.kuzhambu.ai.application.knowledge.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.result.AiInvokeResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiWorkerInvocationApplicationService;
import com.thundax.kuzhambu.ai.application.knowledge.support.KnowledgeAiWorkerUsecaseResolver;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionRequest;
import com.thundax.kuzhambu.ai.domain.knowledge.model.valueobject.KnowledgeAiExtractionResult;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class KnowledgeAiExtractionApplicationServiceImplTest {

    private final KnowledgeAiWorkerUsecaseResolver resolver = new KnowledgeAiWorkerUsecaseResolver();

    @Test
    void extractGraphShouldUseKnowledgeGraphUsecase() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl service =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        KnowledgeAiExtractionResult result = service.extractGraph(request());
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertNotNull(result);
        assertEquals("KNOWLEDGE_GRAPH_EXTRACTION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT.value(), capturedCommand.getCapability());
    }

    @Test
    void extractRelationShouldUseRelationExtractionCapability() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl service =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        service.extractRelations(request());
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("KNOWLEDGE_RELATION_EXTRACTION", capturedCommand.getOperation());
        assertEquals(AiBusinessCapability.KNOWLEDGE_RELATION_EXTRACT.value(), capturedCommand.getCapability());
    }

    @Test
    void extractLineageShouldUseLineageUsecase() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl service =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        service.extractLineage(request());
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("KNOWLEDGE_LINEAGE_EXTRACTION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_LINEAGE_EXTRACT.value(), capturedCommand.getCapability());
    }

    @Test
    void extractTagsShouldUseTagExtractionUsecaseAndCreateCandidate() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl service =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        service.extractTags(request());
        AiInvokeCommand capturedCommand = invocationService.capturedCommand();

        assertEquals("knowledge", capturedCommand.getScope());
        assertEquals("KNOWLEDGE_TAG_EXTRACTION", capturedCommand.getOperation());
        assertNull(capturedCommand.getWorkerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_TAG_EXTRACT.value(), capturedCommand.getCapability());
        assertEquals(true, capturedCommand.isForceJson());
        assertEquals(true, capturedCommand.isCreateCandidate());
    }

    @Test
    void extractGraphShouldPreserveCallAndCandidateIdentifiers() {
        CapturingInvocationService invocationService = new CapturingInvocationService();
        KnowledgeAiExtractionApplicationServiceImpl service =
                new KnowledgeAiExtractionApplicationServiceImpl(invocationService, resolver, null);

        KnowledgeAiExtractionResult result = service.extractGraph(request());

        assertEquals(101L, result.getCallId());
        assertEquals(102L, result.getCandidateId());
        assertEquals("SUCCEEDED", result.getStatus());
    }

    private KnowledgeAiExtractionRequest request() {
        return new KnowledgeAiExtractionRequest(
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
}
