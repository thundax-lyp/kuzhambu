package com.thundax.kuzhambu.ai.application.config.support;

import com.thundax.kuzhambu.ai.application.config.result.PromptCapabilityVariableResult;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PromptCapabilityVariableCatalog {

    private PromptCapabilityVariableCatalog() {}

    public static List<PromptCapabilityVariableResult> list(AiBusinessCapability capability) {
        if (capability == null) {
            return List.of();
        }
        return switch (capability) {
            case CLASSICS_IMAGE_DESCRIBE ->
                variables(
                        variable("title", "内容标题"),
                        variable("contextText", "上下文文本"),
                        variable("imageDescription", "图像识别或人工补充的画面描述"));
            case CLASSICS_IMAGE_GENERATE ->
                variables(
                        variable("title", "内容标题"),
                        variable("sourceText", "用于生成图像的来源文本"),
                        variable("styleGuide", "图像风格与生成约束"));
            case CLASSICS_IMAGE_PROMPT_FUSION ->
                variables(
                        variable("title", "内容标题"), variable("sourceText", "原始文本"),
                        variable("translationText", "译文文本"), variable("imageAnalysis", "图像分析结果"));
            case CLASSICS_QA -> variables(variable("title", "内容标题"), variable("document", "用于问答的文档内容", true));
            case CLASSICS_SPLIT ->
                variables(
                        variable("title", "父条目标题"),
                        variable("sourceText", "待拆分的原文或正文", true),
                        variable("translationText", "已有译文"),
                        variable("splitHint", "拆分粒度或人工提示"));
            case CLASSICS_SUMMARY ->
                variables(
                        variable("contentType", "内容类型", true),
                        variable("title", "内容标题"),
                        variable("categoryPath", "内容所属分类路径"),
                        variable("originalText", "原文内容"),
                        variable("translationText", "译文内容"),
                        variable("bodyText", "正文内容"),
                        variable("existingSummary", "已有摘要"));
            case CLASSICS_TAG_EXTRACT ->
                variables(
                        variable("title", "内容标题"),
                        variable("categoryPath", "内容所属分类路径"),
                        variable("document", "用于生成标签的文档内容", true));
            case CLASSICS_TRANSLATE, CLASSICS_TRANSLATE_BATCH_ITEM ->
                variables(
                        variable("contentType", "内容类型", true), variable("title", "内容标题"),
                        variable("contextPath", "上下文路径"), variable("sourceText", "待翻译的源文本", true));
            case CLASSICS_VISUAL_DESCRIBE ->
                variables(
                        variable("title", "内容标题"),
                        variable("fusionText", "融合后的文本内容"),
                        variable("styleGuide", "视觉描述风格要求"));
            case DISCOVERY_ANSWER_GENERATION ->
                variables(
                        variable("question", "用户问题", true),
                        variable("sources", "检索召回的参考来源", true),
                        variable("answerStyle", "回答风格要求"));
            case DISCOVERY_QUERY_UNDERSTANDING ->
                variables(variable("query", "用户检索查询", true), variable("availableScopes", "可用检索范围"));
            case KNOWLEDGE_GRAPH_EXTRACT ->
                variables(
                        variable("sourceTitle", "来源标题"),
                        variable("sourceText", "待抽取图谱的文本", true),
                        variable("entryRefs", "相关条目引用"));
            case KNOWLEDGE_RELATION_EXTRACT ->
                variables(
                        variable("sourceTitle", "来源标题"),
                        variable("sourceText", "待抽取关系的文本", true),
                        variable("knownEntities", "已知实体列表"));
            case KNOWLEDGE_LINEAGE_EXTRACT ->
                variables(
                        variable("sourceTitle", "来源标题"),
                        variable("sourceText", "待抽取世系的文本", true),
                        variable("lineageHint", "世系范围或人工提示"));
            case KNOWLEDGE_TAG_EXTRACT ->
                variables(
                        variable("sourceTitle", "来源标题"),
                        variable("sourceText", "待抽取标签的文本", true),
                        variable("maxTags", "最大标签数量"));
            case PLATFORM_VERSION_SUMMARY ->
                variables(
                        variable("previousTemplate", "上一版本提示词"),
                        variable("currentTemplate", "当前版本提示词", true),
                        variable("changeSummary", "人工变更摘要"));
            case PROMPT_SUGGEST ->
                variables(
                        variable("template", "当前提示词模板", true),
                        variable("changeGoal", "优化目标"),
                        variable("knownIssues", "已知问题"));
        };
    }

    public static Set<String> names(AiBusinessCapability capability) {
        return list(capability).stream()
                .map(PromptCapabilityVariableResult::variableName)
                .collect(Collectors.toSet());
    }

    public static Map<String, PromptCapabilityVariableResult> byName(AiBusinessCapability capability) {
        return list(capability).stream()
                .collect(Collectors.toMap(PromptCapabilityVariableResult::variableName, Function.identity()));
    }

    private static PromptCapabilityVariableResult variable(String name, String description) {
        return variable(name, description, false);
    }

    private static PromptCapabilityVariableResult variable(String name, String description, boolean required) {
        return new PromptCapabilityVariableResult(name, description, required, 0);
    }

    private static List<PromptCapabilityVariableResult> variables(PromptCapabilityVariableResult... variables) {
        for (int index = 0; index < variables.length; index++) {
            PromptCapabilityVariableResult variable = variables[index];
            variables[index] = new PromptCapabilityVariableResult(
                    variable.variableName(), variable.description(), variable.required(), index + 1);
        }
        return List.of(variables);
    }
}
