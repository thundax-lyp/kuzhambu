package com.thundax.kuzhambu.knowledge.application.workbench.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.support.KnowledgeGraphEntityTypes;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeGraphManuscriptPayloadBuilder {

    private static final String DEFAULT_LOCALE = "zh-CN";
    private static final String DEFAULT_SCOPE_TYPE = "CLASSICS_MANUSCRIPT";

    private final ClassicsFacade classicsFacade;
    private final ObjectMapper objectMapper;

    public KnowledgeGraphManuscriptPayloadBuilder(ClassicsFacade classicsFacade, ObjectMapper objectMapper) {
        this.classicsFacade = classicsFacade;
        this.objectMapper = objectMapper;
    }

    public ManuscriptExtractionPayload build(String sourceContentType, Long sourceContentId, String taskType) {
        validate(sourceContentType, sourceContentId, taskType);
        ClassicsQaKnowledgeFacadeDto manuscript = loadManuscript(sourceContentType, sourceContentId);
        return new ManuscriptExtractionPayload(
                DEFAULT_SCOPE_TYPE,
                writeJson(Map.of(
                        "sourceContentType",
                        sourceContentType,
                        "sourceContentId",
                        sourceContentId,
                        "taskType",
                        taskType,
                        "sourcePath",
                        nullToBlank(manuscript.getSourcePath()))),
                sourceContentType,
                sourceContentId,
                null,
                null,
                nextEventId("graph-workbench"),
                nextEventId("graph-trace"),
                writePromptMessages(taskType),
                writeInputPayload(manuscript, taskType),
                writeOutputSchema(),
                true,
                DEFAULT_LOCALE);
    }

    private ClassicsQaKnowledgeFacadeDto loadManuscript(String sourceContentType, Long sourceContentId) {
        ClassicsQaKnowledgeFacadeResponse response =
                classicsFacade.getWorkbenchQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                        .contentType(sourceContentType)
                        .contentId(String.valueOf(sourceContentId))
                        .build());
        ClassicsQaKnowledgeFacadeDto content = response == null ? null : response.getKnowledge();
        if (content == null) {
            throw new BizException(
                    "Knowledge graph manuscript not found: " + sourceContentType + "#" + sourceContentId);
        }
        return content;
    }

    private String writePromptMessages(String taskType) {
        return writeJson(List.of(
                Map.of(
                        "role",
                        "system",
                        "content",
                        "你是知识图谱抽取助手。请从古籍稿件中抽取结构化实体、关系和世系信息。实体类型必须使用固定枚举："
                                + String.join("、", KnowledgeGraphEntityTypes.VALUES)
                                + "。无法判断实体类型时使用“"
                                + KnowledgeGraphEntityTypes.OTHER
                                + "”。"),
                Map.of(
                        "role",
                        "user",
                        "content",
                        "请根据输入 payload 执行 "
                                + taskType
                                + " 抽取，并返回 JSON。entities 每一项必须包含 name/entityType/description，entityType 只能取固定枚举。")));
    }

    private String writeOutputSchema() {
        return writeJson(Map.of(
                "type",
                "object",
                "properties",
                Map.of(
                        "entities",
                        Map.of(
                                "type",
                                "array",
                                "items",
                                Map.of(
                                        "type",
                                        "object",
                                        "properties",
                                        Map.of(
                                                "name",
                                                Map.of("type", "string"),
                                                "entityType",
                                                Map.of("type", "string", "enum", KnowledgeGraphEntityTypes.VALUES),
                                                "description",
                                                Map.of("type", "string")),
                                        "required",
                                        List.of("name", "entityType"))),
                        "relations",
                        Map.of(
                                "type",
                                "array",
                                "items",
                                Map.of(
                                        "type",
                                        "object",
                                        "properties",
                                        Map.of(
                                                "sourceName",
                                                Map.of("type", "string"),
                                                "targetName",
                                                Map.of("type", "string"),
                                                "relationType",
                                                Map.of("type", "string"),
                                                "evidence",
                                                Map.of("type", "string")),
                                        "required",
                                        List.of("sourceName", "targetName", "relationType"))),
                        "entryRefs",
                        Map.of("type", "array", "items", Map.of("type", "object")),
                        "warnings",
                        Map.of("type", "array", "items", Map.of("type", "string"))),
                "required",
                List.of("entities", "relations")));
    }

    private String writeInputPayload(ClassicsQaKnowledgeFacadeDto manuscript, String taskType) {
        String sourceTitle = nullToBlank(manuscript.getTitle());
        String sourceText = sourceText(manuscript);
        return writeJson(Map.ofEntries(
                Map.entry("taskType", taskType),
                Map.entry("title", sourceTitle),
                Map.entry("content", sourceText),
                Map.entry("sourceTitle", sourceTitle),
                Map.entry("sourceText", sourceText),
                Map.entry(
                        "entryRefs",
                        List.of(Map.of(
                                "contentType",
                                nullToBlank(manuscript.getContentType()),
                                "contentId",
                                nullToBlank(manuscript.getContentId()),
                                "title",
                                nullToBlank(manuscript.getTitle())))),
                Map.entry("knownEntities", List.of()),
                Map.entry("lineageHint", nullToBlank(manuscript.getCategoryPath())),
                Map.entry(
                        "source",
                        Map.of(
                                "sourceId",
                                nullToBlank(manuscript.getSourceId()),
                                "contentType",
                                nullToBlank(manuscript.getContentType()),
                                "contentId",
                                nullToBlank(manuscript.getContentId()),
                                "title",
                                nullToBlank(manuscript.getTitle()),
                                "categoryPath",
                                nullToBlank(manuscript.getCategoryPath()),
                                "summary",
                                nullToBlank(manuscript.getSummary()))),
                Map.entry(
                        "text",
                        Map.of(
                                "body",
                                nullToBlank(manuscript.getBody()),
                                "originalText",
                                nullToBlank(manuscript.getOriginalText()),
                                "translationText",
                                nullToBlank(manuscript.getTranslationText()),
                                "originalExcerpts",
                                nullToBlank(manuscript.getOriginalExcerpts()))),
                Map.entry("tags", manuscript.getTags() == null ? List.of() : manuscript.getTags()),
                Map.entry("qaPairs", manuscript.getQaPairs() == null ? List.of() : manuscript.getQaPairs())));
    }

    private String sourceText(ClassicsQaKnowledgeFacadeDto manuscript) {
        return List.of(
                        nullToBlank(manuscript.getBody()),
                        nullToBlank(manuscript.getOriginalText()),
                        nullToBlank(manuscript.getTranslationText()),
                        nullToBlank(manuscript.getOriginalExcerpts()))
                .stream()
                .filter(value -> !isBlank(value))
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse(nullToBlank(manuscript.getSummary()));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BizException("Knowledge graph manuscript payload serialization failed");
        }
    }

    private void validate(String sourceContentType, Long sourceContentId, String taskType) {
        if (isBlank(sourceContentType) || sourceContentId == null || isBlank(taskType)) {
            throw new BizException("Knowledge graph manuscript extraction request is incomplete");
        }
    }

    private String nextEventId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record ManuscriptExtractionPayload(
            String scopeType,
            String scopeJson,
            String sourceContentType,
            Long sourceContentId,
            Long modelId,
            String modelName,
            String requestId,
            String traceId,
            String promptMessagesJson,
            String inputPayloadJson,
            String outputSchemaJson,
            boolean forceJson,
            String locale) {}
}
