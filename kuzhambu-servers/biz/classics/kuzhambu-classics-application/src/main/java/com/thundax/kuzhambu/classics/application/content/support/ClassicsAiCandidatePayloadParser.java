package com.thundax.kuzhambu.classics.application.content.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassicsAiCandidatePayloadParser {

    private static final Pattern JSON_CODE_FENCE_PATTERN = Pattern.compile(
            "^```[ \\t]*(?:json)?[ \\t]*(?:\\r?\\n)?(?<body>.*?)(?:\\r?\\n)?```[ \\t]*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final ObjectMapper objectMapper;

    public ClassicsAiCandidatePayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String parseText(String resultPayload) {
        if (resultPayload == null || resultPayload.trim().isEmpty()) {
            throw new BizException("AI候选内容为空");
        }
        return resultPayload.trim();
    }

    public String parseSummary(String resultPayload) {
        if (resultPayload == null || resultPayload.trim().isEmpty()) {
            throw new BizException("AI候选内容为空");
        }

        JsonNode root = parseJson(resultPayload, false);
        if (root == null || root.isTextual()) {
            return parseText(resultPayload);
        }

        if (root.isObject()) {
            JsonNode summaryNode = ((ObjectNode) root).get("summary");
            if (summaryNode != null && summaryNode.isTextual()) {
                String summary = summaryNode.asText().trim();
                if (!summary.isEmpty()) {
                    return summary;
                }
            }
            if (summaryNode != null && !summaryNode.isMissingNode() && !summaryNode.isNull()) {
                throw new BizException("AI候选摘要格式错误");
            }
        }

        throw new BizException("AI候选内容为空");
    }

    public List<String> parseTags(String resultPayload) {
        return parseTags(resultPayload, true);
    }

    public List<String> parseTagsIfPresent(String resultPayload) {
        return parseTags(resultPayload, false);
    }

    private List<String> parseTags(String resultPayload, boolean required) {
        JsonNode root = parseJson(resultPayload, required);
        if (root == null) {
            return List.of();
        }
        List<String> tags = new ArrayList<>();
        if (root.isObject()) {
            JsonNode tagsNode = ((ObjectNode) root).get("tags");
            if (tagsNode instanceof ArrayNode) {
                for (JsonNode item : tagsNode) {
                    addStringValue(item, tags);
                }
            }
        } else if (root.isArray()) {
            for (JsonNode item : root) {
                addStringValue(item, tags);
            }
        }

        List<String> distinctTags = dedupe(tags);
        if (required && distinctTags.isEmpty()) {
            throw new BizException("AI候选标签为空");
        }
        return distinctTags;
    }

    public List<AiCandidateQaPairPayload> parseQaPairs(String resultPayload) {
        return parseQaPairs(resultPayload, true);
    }

    public List<AiCandidateQaPairPayload> parseQaPairsIfPresent(String resultPayload) {
        return parseQaPairs(resultPayload, false);
    }

    private List<AiCandidateQaPairPayload> parseQaPairs(String resultPayload, boolean required) {
        JsonNode root = parseJson(resultPayload, required);
        if (root == null) {
            return List.of();
        }
        List<AiCandidateQaPairPayload> pairs = new ArrayList<>();
        if (root.isObject()) {
            JsonNode pairsNode = firstPresent((ObjectNode) root, "qaPairs", "qa_pairs");
            if (pairsNode instanceof ArrayNode) {
                collectQaPairs(pairsNode, pairs);
            }
        } else if (root.isArray()) {
            collectQaPairs(root, pairs);
        }
        List<AiCandidateQaPairPayload> deduped = dedupeQaPairs(pairs);
        if (required && deduped.isEmpty()) {
            throw new BizException("AI候选问答为空");
        }
        return deduped;
    }

    public Long parseStorageObjectId(String resultPayload) {
        JsonNode root = parseJson(resultPayload, true);
        JsonNode storageObjectIdNode = root.path("storageObjectId");
        if (!storageObjectIdNode.canConvertToLong()) {
            throw new BizException("AI候选生图结果缺少 storageObjectId");
        }
        long storageObjectId = storageObjectIdNode.asLong();
        if (storageObjectId <= 0) {
            throw new BizException("AI候选生图结果 storageObjectId 无效");
        }
        return storageObjectId;
    }

    private JsonNode parseJson(String resultPayload, boolean required) {
        try {
            return objectMapper.readTree(normalizeJsonText(resultPayload));
        } catch (Exception ex) {
            if (!required) {
                return null;
            }
            throw new BizException("AI候选内容不是合法JSON");
        }
    }

    private String normalizeJsonText(String resultPayload) {
        String trimmedPayload = resultPayload == null ? "" : resultPayload.trim();
        Matcher matcher = JSON_CODE_FENCE_PATTERN.matcher(trimmedPayload);
        if (!matcher.matches()) {
            return trimmedPayload;
        }
        return matcher.group("body").trim();
    }

    private JsonNode firstPresent(ObjectNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = root.get(fieldName);
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                return node;
            }
        }
        return null;
    }

    private void collectQaPairs(JsonNode pairsNode, List<AiCandidateQaPairPayload> pairs) {
        if (!pairsNode.isArray()) {
            return;
        }
        for (JsonNode item : pairsNode) {
            if (!item.isObject()) {
                continue;
            }
            ObjectNode pairNode = (ObjectNode) item;
            JsonNode questionNode = pairNode.get("question");
            JsonNode answerNode = pairNode.get("answer");
            if (!(questionNode instanceof TextNode) || !(answerNode instanceof TextNode)) {
                continue;
            }
            String question = questionNode.asText().trim();
            String answer = answerNode.asText().trim();
            if (question.isEmpty() || answer.isEmpty()) {
                continue;
            }
            AiCandidateQaPairPayload payload = new AiCandidateQaPairPayload();
            payload.setQuestion(question);
            payload.setAnswer(answer);
            pairs.add(payload);
        }
    }

    private void addStringValue(JsonNode item, List<String> values) {
        if (item instanceof TextNode) {
            String value = item.asText().trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
    }

    private List<String> dedupe(List<String> values) {
        Set<String> seen = new LinkedHashSet<>();
        seen.addAll(values);
        return new ArrayList<>(seen);
    }

    private List<AiCandidateQaPairPayload> dedupeQaPairs(List<AiCandidateQaPairPayload> pairs) {
        Set<String> seen = new LinkedHashSet<>();
        List<AiCandidateQaPairPayload> deduped = new ArrayList<>();
        for (AiCandidateQaPairPayload pair : pairs) {
            String key = pair.getQuestion() + "\n" + pair.getAnswer();
            if (seen.add(key)) {
                deduped.add(pair);
            }
        }
        return deduped;
    }
}
