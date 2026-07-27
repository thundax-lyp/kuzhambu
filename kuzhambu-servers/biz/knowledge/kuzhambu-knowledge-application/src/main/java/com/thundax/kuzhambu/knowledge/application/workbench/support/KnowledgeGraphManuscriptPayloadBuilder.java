package com.thundax.kuzhambu.knowledge.application.workbench.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeGraphManuscriptPayloadBuilder {

    private static final Long DEFAULT_MODEL_ID = 1L;
    private static final String DEFAULT_MODEL_NAME = "gpt-5.5";
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
                DEFAULT_MODEL_ID,
                DEFAULT_MODEL_NAME,
                nextEventId("graph-workbench"),
                nextEventId("graph-trace"),
                writePromptMessages(taskType),
                writeInputPayload(manuscript, taskType),
                writeOutputSchema(taskType),
                true,
                DEFAULT_LOCALE);
    }

    private ClassicsQaKnowledgeFacadeDto loadManuscript(String sourceContentType, Long sourceContentId) {
        ClassicsQaKnowledgeFacadeResponse response =
                classicsFacade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
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
                Map.of("role", "system", "content", "你是知识图谱抽取助手。请从古籍稿件中抽取结构化实体、关系和世系信息。"),
                Map.of("role", "user", "content", "请根据输入 payload 执行 " + taskType + " 抽取，并返回 JSON。")));
    }

    private String writeInputPayload(ClassicsQaKnowledgeFacadeDto manuscript, String taskType) {
        return writeJson(Map.of(
                "taskType",
                taskType,
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
                        nullToBlank(manuscript.getSummary())),
                "text",
                Map.of(
                        "body",
                        nullToBlank(manuscript.getBody()),
                        "originalText",
                        nullToBlank(manuscript.getOriginalText()),
                        "translationText",
                        nullToBlank(manuscript.getTranslationText()),
                        "originalExcerpts",
                        nullToBlank(manuscript.getOriginalExcerpts())),
                "tags",
                manuscript.getTags() == null ? List.of() : manuscript.getTags(),
                "qaPairs",
                manuscript.getQaPairs() == null ? List.of() : manuscript.getQaPairs()));
    }

    private String writeOutputSchema(String taskType) {
        return writeJson(Map.of(
                "taskType",
                taskType,
                "format",
                "knowledge_graph_candidate",
                "required",
                List.of("entities", "relations", "lineageNodes", "lineageRelations")));
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
