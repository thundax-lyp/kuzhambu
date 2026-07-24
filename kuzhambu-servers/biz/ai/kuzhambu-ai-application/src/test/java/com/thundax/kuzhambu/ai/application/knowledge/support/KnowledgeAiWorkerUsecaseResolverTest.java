package com.thundax.kuzhambu.ai.application.knowledge.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

class KnowledgeAiWorkerUsecaseResolverTest {

    private final KnowledgeAiWorkerUsecaseResolver resolver = new KnowledgeAiWorkerUsecaseResolver();

    @Test
    void resolveRelationTask() {
        KnowledgeAiWorkerUsecaseSpec spec = resolver.resolve("RELATION");
        assertNotNull(spec);
        assertEquals("KNOWLEDGE_RELATION_EXTRACTION", spec.operation());
        assertNull(spec.workerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_RELATION_EXTRACT.value(), spec.capability());
    }

    @Test
    void resolveGraphTask() {
        KnowledgeAiWorkerUsecaseSpec spec = resolver.resolve("GRAPH");
        assertNotNull(spec);
        assertEquals("KNOWLEDGE_GRAPH_EXTRACTION", spec.operation());
        assertNull(spec.workerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT.value(), spec.capability());
    }

    @Test
    void resolveTagTask() {
        KnowledgeAiWorkerUsecaseSpec spec = resolver.resolve("TAG");
        assertNotNull(spec);
        assertEquals("KNOWLEDGE_TAG_EXTRACTION", spec.operation());
        assertNull(spec.workerPath());
        assertEquals(AiBusinessCapability.KNOWLEDGE_TAG_EXTRACT.value(), spec.capability());
    }

    @Test
    void resolveUnsupportedTaskShouldThrow() {
        BizException exception = assertThrows(BizException.class, () -> resolver.resolve("UNKNOWN"));
        assertEquals("unsupported knowledge ai worker usecase: taskType=UNKNOWN", exception.getMessage());
    }
}
