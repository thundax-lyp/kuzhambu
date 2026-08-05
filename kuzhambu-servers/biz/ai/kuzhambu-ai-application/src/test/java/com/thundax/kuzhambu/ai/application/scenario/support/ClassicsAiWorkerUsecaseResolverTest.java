package com.thundax.kuzhambu.ai.application.scenario.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

class ClassicsAiWorkerUsecaseResolverTest {

    private final ClassicsAiWorkerUsecaseResolver resolver = new ClassicsAiWorkerUsecaseResolver();

    @Test
    void resolveSanCaiSummary() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "CLASSICS_SUMMARY");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_SUMMARY", spec.operation());
        assertNull(spec.workerPath());
        assertEquals("summary", spec.workerCapability());
    }

    @Test
    void resolveSanCaiTranslateBatchItem() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "CLASSICS_TRANSLATE_BATCH_ITEM");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM", spec.operation());
        assertNull(spec.workerPath());
    }

    @Test
    void resolveWangqiQa() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("WANGQI_DOCUMENT", "CLASSICS_QA");
        assertNotNull(spec);
        assertEquals("CLASSICS_WANGQI_QA", spec.operation());
        assertNull(spec.workerPath());
    }

    @Test
    void resolveMingCustomsTags() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("MING_CUSTOMS", "CLASSICS_TAG_EXTRACT");
        assertNotNull(spec);
        assertEquals("CLASSICS_MING_CUSTOMS_TAGS", spec.operation());
        assertNull(spec.workerPath());
    }

    @Test
    void resolveUnsupportedWangqiTranslateShouldThrow() {
        BizException exception =
                assertThrows(BizException.class, () -> resolver.resolve("WANGQI_DOCUMENT", "CLASSICS_TRANSLATE"));
        assertEquals(
                "unsupported classics ai worker usecase: contentType=WANGQI_DOCUMENT, capability=CLASSICS_TRANSLATE",
                exception.getMessage());
    }

    @Test
    void resolveUnsupportedMingCustomsVisualShouldThrow() {
        BizException exception =
                assertThrows(BizException.class, () -> resolver.resolve("MING_CUSTOMS", "CLASSICS_VISUAL_DESCRIBE"));
        assertEquals(
                "unsupported classics ai worker usecase: contentType=MING_CUSTOMS, capability=CLASSICS_VISUAL_DESCRIBE",
                exception.getMessage());
    }

    @Test
    void resolveSanCaiImageAnalysis() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "CLASSICS_IMAGE_DESCRIBE");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_IMAGE_ANALYSIS", spec.operation());
        assertNull(spec.workerPath());
        assertEquals("image_analysis", spec.workerCapability());
    }

    @Test
    void resolveSanCaiFusion() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "CLASSICS_IMAGE_PROMPT_FUSION");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_FUSION", spec.operation());
        assertNull(spec.workerPath());
    }

    @Test
    void resolveSanCaiImageGen() {
        ClassicsAiWorkerUsecaseSpec spec = resolver.resolve("SANCAI_ENTRY", "CLASSICS_IMAGE_GENERATE");
        assertNotNull(spec);
        assertEquals("CLASSICS_SANCAI_IMAGE_GEN", spec.operation());
        assertNull(spec.workerPath());
        assertEquals("image_gen", spec.workerCapability());
    }
}
