package com.thundax.kuzhambu.ai.application.refinement.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;

public class ClassicsAiWorkerUsecaseResolver {

    private static final Map<String, Map<String, ClassicsAiWorkerUsecaseSpec>> SUPPORTED_USECASES = Map.of(
            "SANCAI_ENTRY",
            Map.of(
                    "translate",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_TRANSLATE", "/internal/ai/classics/sancai/translate"),
                    "translate_batch_item",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM",
                                    "/internal/ai/classics/sancai/translate-batch-item"),
                    "summary",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_SUMMARY", "/internal/ai/classics/sancai/summary"),
                    "tags",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_TAGS", "/internal/ai/classics/sancai/tags"),
                    "qa", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_QA", "/internal/ai/classics/sancai/qa"),
                    "image_analysis",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_IMAGE_ANALYSIS", "/internal/ai/classics/sancai/image-analysis"),
                    "image_gen",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_IMAGE_GEN", "/internal/ai/classics/sancai/image-gen"),
                    "fusion",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_FUSION", "/internal/ai/classics/sancai/fusion"),
                    "visual",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_VISUAL_DESCRIPTION",
                                    "/internal/ai/classics/sancai/visual-description"),
                    "split",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_SANCAI_SPLIT", "/internal/ai/classics/sancai/split")),
            "WANGQI_DOCUMENT",
            Map.of(
                    "summary",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_WANGQI_SUMMARY", "/internal/ai/classics/wangqi/summary"),
                    "tags",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_WANGQI_TAGS", "/internal/ai/classics/wangqi/tags"),
                    "qa", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_QA", "/internal/ai/classics/wangqi/qa")),
            "MING_CUSTOMS",
            Map.of(
                    "summary",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_MING_CUSTOMS_SUMMARY", "/internal/ai/classics/ming-customs/summary"),
                    "tags",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_MING_CUSTOMS_TAGS", "/internal/ai/classics/ming-customs/tags"),
                    "qa",
                            new ClassicsAiWorkerUsecaseSpec(
                                    "CLASSICS_MING_CUSTOMS_QA", "/internal/ai/classics/ming-customs/qa")));

    public ClassicsAiWorkerUsecaseSpec resolve(String contentType, String capability) {
        if (!SUPPORTED_USECASES.containsKey(contentType)
                || !SUPPORTED_USECASES.get(contentType).containsKey(capability)) {
            throw new BizException("unsupported classics ai worker usecase: contentType=%s, capability=%s"
                    .formatted(contentType, capability));
        }
        return SUPPORTED_USECASES.get(contentType).get(capability);
    }
}
