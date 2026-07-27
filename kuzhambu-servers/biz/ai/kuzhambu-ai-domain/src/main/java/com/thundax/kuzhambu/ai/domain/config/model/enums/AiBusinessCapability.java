package com.thundax.kuzhambu.ai.domain.config.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;
import java.util.List;

public enum AiBusinessCapability {
    CLASSICS_TRANSLATE("classics_translate", "古籍翻译"),
    CLASSICS_TRANSLATE_BATCH_ITEM("classics_translate_batch_item", "古籍批量翻译项"),
    CLASSICS_SUMMARY("classics_summary", "古籍摘要"),
    CLASSICS_TAG_EXTRACT("classics_tags", "古籍标签提取"),
    CLASSICS_QA("classics_qa", "古籍问答生成"),
    CLASSICS_SPLIT("classics_split", "古籍条目拆分"),
    CLASSICS_IMAGE_DESCRIBE("classics_image_describe", "古籍图片理解"),
    CLASSICS_IMAGE_PROMPT_FUSION("classics_image_prompt_fusion", "古籍图文提示词融合"),
    CLASSICS_VISUAL_DESCRIBE("classics_visual_describe", "古籍视觉描述"),
    CLASSICS_IMAGE_GENERATE("classics_image_generate", "古籍图片生成"),
    DISCOVERY_QUERY_UNDERSTANDING("discovery_query_understanding", "知识发现查询理解"),
    DISCOVERY_ANSWER_GENERATION("discovery_answer_generation", "知识发现回答生成"),
    KNOWLEDGE_GRAPH_EXTRACT("knowledge_graph_extract", "知识图谱抽取"),
    KNOWLEDGE_RELATION_EXTRACT("knowledge_relation_extract", "实体关系抽取"),
    KNOWLEDGE_LINEAGE_EXTRACT("knowledge_lineage_extract", "世系图抽取"),
    KNOWLEDGE_TAG_EXTRACT("knowledge_tags", "知识标签提取"),
    PLATFORM_VERSION_SUMMARY("platform_version_summary", "版本摘要"),
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

    public List<AiModelCapability> requiredModelCapabilities() {
        return switch (this) {
            case CLASSICS_IMAGE_DESCRIBE -> List.of(AiModelCapability.IMAGE2TEXT);
            case CLASSICS_IMAGE_GENERATE -> List.of(AiModelCapability.TEXT2IMAGE);
            default -> List.of(AiModelCapability.TEXT2TEXT);
        };
    }

    public static AiBusinessCapability from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value) || item.code.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-10001", "ai.business-capability.invalid", "Unknown AI business capability: " + value));
    }

    public static AiBusinessCapability fromAlias(String value) {
        return from(normalizeAlias(value));
    }

    private static String normalizeAlias(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "translate" -> CLASSICS_TRANSLATE.value();
            case "summary" -> CLASSICS_SUMMARY.value();
            case "tags" -> CLASSICS_TAG_EXTRACT.value();
            case "qa" -> CLASSICS_QA.value();
            case "image_analysis" -> CLASSICS_IMAGE_DESCRIBE.value();
            case "visual" -> CLASSICS_VISUAL_DESCRIBE.value();
            case "image_gen" -> CLASSICS_IMAGE_GENERATE.value();
            case "split" -> CLASSICS_SPLIT.value();
            case "relation_extraction" -> KNOWLEDGE_RELATION_EXTRACT.value();
            case "knowledge_graph" -> KNOWLEDGE_GRAPH_EXTRACT.value();
            case "lineage_extraction" -> KNOWLEDGE_LINEAGE_EXTRACT.value();
            default -> value;
        };
    }
}
