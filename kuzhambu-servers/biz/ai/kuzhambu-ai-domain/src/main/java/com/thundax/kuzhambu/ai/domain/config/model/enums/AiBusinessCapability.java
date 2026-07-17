package com.thundax.kuzhambu.ai.domain.config.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;

public enum AiBusinessCapability {
    TRANSLATE("translate", "古文翻译"),
    SUMMARY("summary", "摘要生成"),
    VERSION_SUMMARY("version_summary", "版本摘要"),
    TAG_EXTRACT("tags", "标签提取"),
    QA("qa", "问答生成"),
    SPLIT("split", "条目拆分"),
    VISUAL("visual", "视觉描述"),
    FUSION("fusion", "信息融合"),
    IMAGE_DESCRIBE("image_analysis", "图片理解"),
    IMAGE_PROMPT_FUSION("image_prompt_fusion", "图文提示词融合"),
    IMAGE_GENERATE("image_gen", "图片生成"),
    QUERY_UNDERSTANDING("query_understanding", "查询理解"),
    ANSWER_GENERATION("answer_generation", "回答生成"),
    KNOWLEDGE_GRAPH_EXTRACT("knowledge_graph", "知识图谱抽取"),
    RELATION_EXTRACT("relation_extraction", "实体关系抽取"),
    LINEAGE_EXTRACT("lineage_extraction", "世系图抽取"),
    PROMPT_SUGGEST("prompt_suggestion", "提示词优化建议");

    private final String code;
    private final String displayName;

    AiBusinessCapability(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String value() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public static AiBusinessCapability from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value) || item.code.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-10001", "ai.business-capability.invalid", "Unknown AI business capability: " + value));
    }
}
