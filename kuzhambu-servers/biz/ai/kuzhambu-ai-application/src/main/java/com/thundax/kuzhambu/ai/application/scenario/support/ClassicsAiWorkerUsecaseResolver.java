package com.thundax.kuzhambu.ai.application.scenario.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ClassicsAiWorkerUsecaseResolver {

    private static final Map<String, Map<String, ClassicsAiWorkerUsecaseSpec>> SUPPORTED_USECASES = Map.of(
            "SANCAI_ENTRY",
            Map.of(
                    "CLASSICS_TRANSLATE",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TRANSLATE", null, "translate"),
                    "CLASSICS_TRANSLATE_BATCH_ITEM",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM", null, "translate"),
                    "CLASSICS_SUMMARY", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_SUMMARY", null, "summary"),
                    "CLASSICS_TAG_EXTRACT", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TAGS", null, "tags"),
                    "CLASSICS_QA", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_QA", null, "qa"),
                    "CLASSICS_IMAGE_DESCRIBE",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_IMAGE_ANALYSIS", null, "image_analysis"),
                    "CLASSICS_IMAGE_GENERATE",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_IMAGE_GEN", null, "image_gen"),
                    "CLASSICS_IMAGE_PROMPT_FUSION",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_FUSION", null, "fusion"),
                    "CLASSICS_VISUAL_DESCRIBE",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_VISUAL_DESCRIPTION", null, "visual"),
                    "CLASSICS_SPLIT", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_SPLIT", null, "split")),
            "WANGQI_DOCUMENT",
            Map.of(
                    "CLASSICS_SUMMARY", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_SUMMARY", null, "summary"),
                    "CLASSICS_TAG_EXTRACT", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_TAGS", null, "tags"),
                    "CLASSICS_QA", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_QA", null, "qa")),
            "MING_CUSTOMS",
            Map.of(
                    "CLASSICS_SUMMARY",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_SUMMARY", null, "summary"),
                    "CLASSICS_TAG_EXTRACT", new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_TAGS", null, "tags"),
                    "CLASSICS_QA", new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_QA", null, "qa")));

    public ClassicsAiWorkerUsecaseSpec resolve(String contentType, String capability) {
        if (!SUPPORTED_USECASES.containsKey(contentType)
                || !SUPPORTED_USECASES.get(contentType).containsKey(capability)) {
            throw new BizException("unsupported classics ai worker usecase: contentType=%s, capability=%s"
                    .formatted(contentType, capability));
        }
        return SUPPORTED_USECASES.get(contentType).get(capability);
    }
}
