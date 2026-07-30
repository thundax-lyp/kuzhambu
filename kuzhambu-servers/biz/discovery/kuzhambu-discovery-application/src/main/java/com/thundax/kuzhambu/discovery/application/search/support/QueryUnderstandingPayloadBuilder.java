package com.thundax.kuzhambu.discovery.application.search.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QueryUnderstandingPayloadBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String buildPromptMessagesJson(
            SearchQuery query,
            String normalizedQueryText,
            com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement) {
        Map<String, Object> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a discovery query understanding assistant.");

        Map<String, Object> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", query.getQueryText());

        return writeJson(List.of(systemMessage, userMessage));
    }

    public String buildInputPayloadJson(
            SearchQuery query,
            String normalizedQueryText,
            com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("queryText", query.getQueryText());
        payload.put("normalizedQueryText", normalizedQueryText);
        payload.put("knowledgeBases", query.getKnowledgeBases());
        payload.put("categoryCodes", query.getCategoryCodes());
        payload.put("tagNames", query.getTagNames());
        payload.put("contentStatuses", query.getContentStatuses());
        payload.put("visibilityScopes", query.getVisibilityScopes());
        payload.put("tagHint", enhancement.tagHint());
        payload.put("recognizedEntities", enhancement.recognizedEntities());
        payload.put("requestId", query.getRequestId());
        payload.put("traceId", query.getTraceId());
        return writeJson(payload);
    }

    public String buildOutputSchemaJson() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("intent", "rewrittenQueryText", "recognizedEntities"));
        schema.put(
                "properties",
                Map.of(
                        "intent", Map.of("type", "string"),
                        "rewrittenQueryText", Map.of("type", "string"),
                        "recognizedEntities", Map.of("type", "array")));
        return writeJson(schema);
    }

    private String writeJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "DISCOVERY-20003",
                    "discovery.search.query-understanding.payload-build-failed",
                    "Query understanding payload build failed",
                    exception);
        }
    }
}
