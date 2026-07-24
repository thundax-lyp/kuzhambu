package com.thundax.kuzhambu.ai.application.refinement.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ClassicsAiWorkerUsecaseResolver {

    private static final Map<String, Map<String, ClassicsAiWorkerUsecaseSpec>> SUPPORTED_USECASES = Map.of(
            "SANCAI_ENTRY",
            Map.of(
                    "classics_translate",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TRANSLATE", null, "translate"),
                    "classics_translate_batch_item",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM", null, "translate"),
                    "classics_summary", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_SUMMARY", null, "summary"),
                    "classics_tags", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TAGS", null, "tags"),
                    "classics_qa", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_QA", null, "qa"),
                    "classics_image_describe",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_IMAGE_ANALYSIS", null, "image_analysis"),
                    "classics_image_generate",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_IMAGE_GEN", null, "image_gen"),
                    "classics_image_prompt_fusion",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_FUSION", null, "fusion"),
                    "classics_visual_describe",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_VISUAL_DESCRIPTION", null, "visual"),
                    "classics_split", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_SPLIT", null, "split")),
            "WANGQI_DOCUMENT",
            Map.of(
                    "classics_summary", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_SUMMARY", null, "summary"),
                    "classics_tags", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_TAGS", null, "tags"),
                    "classics_qa", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_QA", null, "qa")),
            "MING_CUSTOMS",
            Map.of(
                    "classics_summary",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_SUMMARY", null, "summary"),
                    "classics_tags", new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_TAGS", null, "tags"),
                    "classics_qa", new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_QA", null, "qa")));

    public ClassicsAiWorkerUsecaseSpec resolve(String contentType, String capability) {
        if (!SUPPORTED_USECASES.containsKey(contentType)
                || !SUPPORTED_USECASES.get(contentType).containsKey(capability)) {
            throw new BizException("unsupported classics ai worker usecase: contentType=%s, capability=%s"
                    .formatted(contentType, capability));
        }
        return SUPPORTED_USECASES.get(contentType).get(capability);
    }
}
