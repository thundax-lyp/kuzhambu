package com.thundax.kuzhambu.ai.application.platform.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PlatformAiWorkerUsecaseResolver {

    private static final Map<String, PlatformAiWorkerUsecaseSpec> SUPPORTED_USECASES = Map.of(
            "PLATFORM_PROMPT_SUGGESTION",
            new PlatformAiWorkerUsecaseSpec(
                    "PLATFORM_PROMPT_SUGGESTION", "/internal/ai/platform/prompt-suggestion", "prompt_suggestion", true),
            "PLATFORM_VERSION_SUMMARY",
            new PlatformAiWorkerUsecaseSpec(
                    "PLATFORM_VERSION_SUMMARY", "/internal/ai/platform/version-summary", "version_summary", false));

    public PlatformAiWorkerUsecaseSpec resolve(String usecase) {
        PlatformAiWorkerUsecaseSpec spec = SUPPORTED_USECASES.get(usecase);
        if (spec == null) {
            throw new BizException("unsupported platform ai worker usecase: usecase=" + usecase);
        }
        return spec;
    }
}
