package com.thundax.kuzhambu.ai.application.knowledge.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

class KnowledgeAiWorkerUsecaseResolverTest {

    private final KnowledgeAiWorkerUsecaseResolver resolver = new KnowledgeAiWorkerUsecaseResolver();

    @Test
    void resolveRelationTask() {
        KnowledgeAiWorkerUsecaseSpec spec = resolver.resolve("RELATION");
        assertNotNull(spec);
        assertEquals("KNOWLEDGE_RELATION_EXTRACTION", spec.operation());
        assertEquals("/internal/ai/knowledge/relation-extraction", spec.workerPath());
        assertEquals("relation_extraction", spec.capability());
    }

    @Test
    void resolveGraphTask() {
        KnowledgeAiWorkerUsecaseSpec spec = resolver.resolve("GRAPH");
        assertNotNull(spec);
        assertEquals("KNOWLEDGE_GRAPH_EXTRACTION", spec.operation());
        assertEquals("/internal/ai/knowledge/graph-extraction", spec.workerPath());
        assertEquals("knowledge_graph", spec.capability());
    }

    @Test
    void resolveUnsupportedTaskShouldThrow() {
        BizException exception = assertThrows(BizException.class, () -> resolver.resolve("TAG"));
        assertEquals("unsupported knowledge ai worker usecase: taskType=TAG", exception.getMessage());
    }
}
