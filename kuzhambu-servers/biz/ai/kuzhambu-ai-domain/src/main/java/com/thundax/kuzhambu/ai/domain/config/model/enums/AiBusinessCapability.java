package com.thundax.kuzhambu.ai.domain.config.model.enums;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.util.Arrays;
import java.util.List;

public enum AiBusinessCapability {
    /** 古籍翻译 */
    CLASSICS_TRANSLATE,
    /** 古籍批量翻译项 */
    CLASSICS_TRANSLATE_BATCH_ITEM,
    /** 古籍摘要 */
    CLASSICS_SUMMARY,
    /** 古籍标签提取 */
    CLASSICS_TAG_EXTRACT,
    /** 古籍问答生成 */
    CLASSICS_QA,
    /** 古籍条目拆分 */
    CLASSICS_SPLIT,
    /** 古籍图片理解 */
    CLASSICS_IMAGE_DESCRIBE,
    /** 古籍图文提示词融合 */
    CLASSICS_IMAGE_PROMPT_FUSION,
    /** 古籍视觉描述 */
    CLASSICS_VISUAL_DESCRIBE,
    /** 古籍图片生成 */
    CLASSICS_IMAGE_GENERATE,
    /** 知识发现查询理解 */
    DISCOVERY_QUERY_UNDERSTANDING,
    /** 知识发现回答生成 */
    DISCOVERY_ANSWER_GENERATION,
    /** 知识图谱抽取 */
    KNOWLEDGE_GRAPH_EXTRACT,
    /** 实体关系抽取 */
    KNOWLEDGE_RELATION_EXTRACT,
    /** 世系图抽取 */
    KNOWLEDGE_LINEAGE_EXTRACT,
    /** 知识标签提取 */
    KNOWLEDGE_TAG_EXTRACT,
    /** 版本摘要 */
    PLATFORM_VERSION_SUMMARY,
    /** 提示词优化建议 */
    PROMPT_SUGGEST;

    public String value() {
        return name();
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
                .filter(item ->
                        item.name().equalsIgnoreCase(value) || item.legacyCode().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "AI-10001", "ai.business-capability.invalid", "Unknown AI business capability: " + value));
    }

    private String legacyCode() {
        return switch (this) {
            case CLASSICS_TRANSLATE -> "classics_translate";
            case CLASSICS_TRANSLATE_BATCH_ITEM -> "classics_translate_batch_item";
            case CLASSICS_SUMMARY -> "classics_summary";
            case CLASSICS_TAG_EXTRACT -> "classics_tags";
            case CLASSICS_QA -> "classics_qa";
            case CLASSICS_SPLIT -> "classics_split";
            case CLASSICS_IMAGE_DESCRIBE -> "classics_image_describe";
            case CLASSICS_IMAGE_PROMPT_FUSION -> "classics_image_prompt_fusion";
            case CLASSICS_VISUAL_DESCRIBE -> "classics_visual_describe";
            case CLASSICS_IMAGE_GENERATE -> "classics_image_generate";
            case DISCOVERY_QUERY_UNDERSTANDING -> "discovery_query_understanding";
            case DISCOVERY_ANSWER_GENERATION -> "discovery_answer_generation";
            case KNOWLEDGE_GRAPH_EXTRACT -> "knowledge_graph_extract";
            case KNOWLEDGE_RELATION_EXTRACT -> "knowledge_relation_extract";
            case KNOWLEDGE_LINEAGE_EXTRACT -> "knowledge_lineage_extract";
            case KNOWLEDGE_TAG_EXTRACT -> "knowledge_tags";
            case PLATFORM_VERSION_SUMMARY -> "platform_version_summary";
            case PROMPT_SUGGEST -> "prompt_suggestion";
        };
    }
}
