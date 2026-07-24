package com.thundax.kuzhambu.ai.application.platform.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

class PlatformAiWorkerUsecaseResolverTest {

    private final PlatformAiWorkerUsecaseResolver resolver = new PlatformAiWorkerUsecaseResolver();

    @Test
    void resolvePromptSuggestionUsecase() {
        PlatformAiWorkerUsecaseSpec spec = resolver.resolve("PLATFORM_PROMPT_SUGGESTION");

        assertNotNull(spec);
        assertEquals("PLATFORM_PROMPT_SUGGESTION", spec.operation());
        assertNull(spec.workerPath());
        assertEquals(AiBusinessCapability.PROMPT_SUGGEST.value(), spec.capability());
        assertEquals("prompt_suggestion", spec.workerCapability());
        assertTrue(spec.defaultCreateCandidate());
    }

    @Test
    void resolveVersionSummaryUsecase() {
        PlatformAiWorkerUsecaseSpec spec = resolver.resolve("PLATFORM_VERSION_SUMMARY");

        assertNotNull(spec);
        assertEquals("PLATFORM_VERSION_SUMMARY", spec.operation());
        assertNull(spec.workerPath());
        assertEquals(AiBusinessCapability.PLATFORM_VERSION_SUMMARY.value(), spec.capability());
        assertEquals("version_summary", spec.workerCapability());
        assertFalse(spec.defaultCreateCandidate());
    }

    @Test
    void resolveUnsupportedUsecaseShouldThrow() {
        BizException exception = assertThrows(BizException.class, () -> resolver.resolve("PLATFORM_UNKNOWN"));

        assertEquals("unsupported platform ai worker usecase: usecase=PLATFORM_UNKNOWN", exception.getMessage());
    }
}
