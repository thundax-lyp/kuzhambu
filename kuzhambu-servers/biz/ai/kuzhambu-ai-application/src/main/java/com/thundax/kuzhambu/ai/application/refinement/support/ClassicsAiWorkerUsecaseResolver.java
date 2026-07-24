package com.thundax.kuzhambu.ai.application.refinement.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ClassicsAiWorkerUsecaseResolver {

    private static final Map<String, Map<String, ClassicsAiWorkerUsecaseSpec>> SUPPORTED_USECASES = Map.of(
            "SANCAI_ENTRY",
            Map.of(
                    "classics_translate", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TRANSLATE", null),
                    "classics_translate_batch_item",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TRANSLATE_BATCH_ITEM", null),
                    "classics_summary", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_SUMMARY", null),
                    "classics_tags", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_TAGS", null),
                    "classics_qa", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_QA", null),
                    "classics_image_describe", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_IMAGE_ANALYSIS", null),
                    "classics_image_generate", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_IMAGE_GEN", null),
                    "classics_image_prompt_fusion", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_FUSION", null),
                    "classics_visual_describe",
                            new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_VISUAL_DESCRIPTION", null),
                    "classics_split", new ClassicsAiWorkerUsecaseSpec("CLASSICS_SANCAI_SPLIT", null)),
            "WANGQI_DOCUMENT",
            Map.of(
                    "classics_summary", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_SUMMARY", null),
                    "classics_tags", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_TAGS", null),
                    "classics_qa", new ClassicsAiWorkerUsecaseSpec("CLASSICS_WANGQI_QA", null)),
            "MING_CUSTOMS",
            Map.of(
                    "classics_summary", new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_SUMMARY", null),
                    "classics_tags", new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_TAGS", null),
                    "classics_qa", new ClassicsAiWorkerUsecaseSpec("CLASSICS_MING_CUSTOMS_QA", null)));

    public ClassicsAiWorkerUsecaseSpec resolve(String contentType, String capability) {
        if (!SUPPORTED_USECASES.containsKey(contentType)
                || !SUPPORTED_USECASES.get(contentType).containsKey(capability)) {
            throw new BizException("unsupported classics ai worker usecase: contentType=%s, capability=%s"
                    .formatted(contentType, capability));
        }
        return SUPPORTED_USECASES.get(contentType).get(capability);
    }
}
