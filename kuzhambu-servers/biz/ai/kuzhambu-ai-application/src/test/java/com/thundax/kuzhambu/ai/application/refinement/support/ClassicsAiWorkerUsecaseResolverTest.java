package com.thundax.kuzhambu.ai.application.refinement.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

class ClassicsAiWorkerUsecaseResolverTest {

    private final ClassicsAiWorkerUsecaseResolver resolver = new ClassicsAiWorkerUsecaseResolver();

    @Test
    void resolveSanCaiSummary() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "summary");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_SUMMARY", spec.operation());
        assertEquals("/internal/ai/classics/sancai/summary", spec.workerPath());
    }

    @Test
    void resolveWangqiQa() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("WANGQI_DOCUMENT", "qa");
        assertNotNull(spec);
        assertEquals("CLASSICS_WANGQI_QA", spec.operation());
        assertEquals("/internal/ai/classics/wangqi/qa", spec.workerPath());
    }

    @Test
    void resolveMingCustomsTags() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("MING_CUSTOMS", "tags");
        assertNotNull(spec);
        assertEquals("CLASSICS_MING_CUSTOMS_TAGS", spec.operation());
        assertEquals("/internal/ai/classics/ming-customs/tags", spec.workerPath());
    }

    @Test
    void resolveUnsupportedWangqiTranslateShouldThrow() {
        BizException exception =
                assertThrows(BizException.class, () -> resolver.resolve("WANGQI_DOCUMENT", "translate"));
        assertEquals(
                "unsupported classics ai worker usecase: contentType=WANGQI_DOCUMENT, capability=translate",
                exception.getMessage());
    }

    @Test
    void resolveUnsupportedMingCustomsVisualShouldThrow() {
        BizException exception = assertThrows(BizException.class, () -> resolver.resolve("MING_CUSTOMS", "visual"));
        assertEquals(
                "unsupported classics ai worker usecase: contentType=MING_CUSTOMS, capability=visual",
                exception.getMessage());
    }

    @Test
    void resolveSanCaiImageAnalysis() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "image_analysis");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_IMAGE_ANALYSIS", spec.operation());
        assertEquals("/internal/ai/classics/sancai/image-analysis", spec.workerPath());
    }

    @Test
    void resolveSanCaiFusion() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "fusion");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_FUSION", spec.operation());
        assertEquals("/internal/ai/classics/sancai/fusion", spec.workerPath());
    }

    @Test
    void resolveSanCaiImageGen() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "image_gen");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_IMAGE_GEN", spec.operation());
        assertEquals("/internal/ai/classics/sancai/image-gen", spec.workerPath());
    }
}
