package com.thundax.kuzhambu.ai.application.discovery.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

class DiscoveryAiWorkerUsecaseResolverTest {

    private final DiscoveryAiWorkerUsecaseResolver resolver = new DiscoveryAiWorkerUsecaseResolver();

    @Test
    void resolveQueryUnderstandingUsecase() {
        DiscoveryAiWorkerUsecaseSpec spec = resolver.resolve("DISCOVERY_QUERY_UNDERSTANDING");
        assertNotNull(spec);
        assertEquals("DISCOVERY_QUERY_UNDERSTANDING", spec.operation());
        assertEquals("/internal/ai/discovery/query-understanding", spec.workerPath());
        assertEquals("query_understanding", spec.capability());
        assertFalse(spec.stream());
    }

    @Test
    void resolveQueryRewriteUsecase() {
        DiscoveryAiWorkerUsecaseSpec spec = resolver.resolve("DISCOVERY_QUERY_REWRITE");
        assertNotNull(spec);
        assertEquals("DISCOVERY_QUERY_REWRITE", spec.operation());
        assertEquals("/internal/ai/discovery/query-rewrite", spec.workerPath());
        assertEquals("query_understanding", spec.capability());
        assertFalse(spec.stream());
    }

    @Test
    void resolveAnswerGenerationUsecase() {
        DiscoveryAiWorkerUsecaseSpec spec = resolver.resolve("DISCOVERY_ANSWER_GENERATION");
        assertNotNull(spec);
        assertEquals("DISCOVERY_ANSWER_GENERATION", spec.operation());
        assertEquals("/internal/ai/discovery/answer-generation", spec.workerPath());
        assertEquals("answer_generation", spec.capability());
        assertFalse(spec.stream());
    }

    @Test
    void resolveAnswerGenerationStreamUsecase() {
        DiscoveryAiWorkerUsecaseSpec spec = resolver.resolve("DISCOVERY_ANSWER_GENERATION_STREAM");
        assertNotNull(spec);
        assertEquals("DISCOVERY_ANSWER_GENERATION_STREAM", spec.operation());
        assertEquals("/internal/ai/discovery/answer-generation/stream", spec.workerPath());
        assertEquals("answer_generation", spec.capability());
        assertTrue(spec.stream());
    }

    @Test
    void resolveUnsupportedUsecaseShouldThrow() {
        BizException exception = assertThrows(BizException.class, () -> resolver.resolve("DISCOVERY_IMAGE_SUMMARY"));
        assertEquals(
                "unsupported discovery ai worker usecase: usecase=DISCOVERY_IMAGE_SUMMARY", exception.getMessage());
    }
}
