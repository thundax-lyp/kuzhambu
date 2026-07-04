package com.thundax.kuzhambu.discovery.application.qa.support;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.crypto.Sha256Digest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeRevisionCalculator {

    private static final String REVISION_PREFIX = "sha256:";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(NON_NULL);

    public String calculate(KnowledgeDocument document) {
        if (document == null) {
            return buildRevision(Map.of());
        }
        return calculate(document.knowledge());
    }

    public String calculate(KnowledgeDocument.Knowledge knowledge) {
        return buildRevision(normalizeKnowledge(knowledge));
    }

    private String buildRevision(Map<String, Object> payload) {
        String source = stringify(payload);
        return REVISION_PREFIX + Sha256Digest.hashBase64Url(source.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Object> normalizeKnowledge(KnowledgeDocument.Knowledge knowledge) {
        if (knowledge == null) {
            return Map.of();
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, "title", knowledge.title());
        put(payload, "categoryPath", knowledge.categoryPath());
        put(payload, "summary", knowledge.summary());
        put(payload, "body", knowledge.body());
        put(payload, "originalText", knowledge.originalText());
        put(payload, "translationText", knowledge.translationText());
        put(payload, "originalExcerpts", knowledge.originalExcerpts());

        List<String> tags = knowledge.tags() == null
                ? List.of()
                : knowledge.tags().stream().filter(StringUtils::isNotBlank).toList();
        if (!tags.isEmpty()) {
            payload.put("tags", tags);
        }

        List<Map<String, String>> qaPairs = knowledge.qaPairs() == null
                ? List.of()
                : knowledge.qaPairs().stream()
                        .filter(this::isValidQaPair)
                        .map(this::toQaPairRecord)
                        .toList();
        if (!qaPairs.isEmpty()) {
            payload.put("qaPairs", qaPairs);
        }
        return payload;
    }

    private void put(Map<String, Object> payload, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            payload.put(key, value);
        }
    }

    private boolean isValidQaPair(KnowledgeDocument.QaPair qaPair) {
        return qaPair != null && StringUtils.isNotBlank(qaPair.question()) && StringUtils.isNotBlank(qaPair.answer());
    }

    private Map<String, String> toQaPairRecord(KnowledgeDocument.QaPair qaPair) {
        return Map.of("question", qaPair.question(), "answer", qaPair.answer());
    }

    private String stringify(Map<String, Object> payload) {
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "";
        }
    }
}
