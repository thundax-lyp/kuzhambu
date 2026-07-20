package com.thundax.kuzhambu.common.knowledge.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.configure.KuzhambuKnowledgeProperties;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseEnsureRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseListRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBasePageResult;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatChoice;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatMessage;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatResult;
import com.thundax.kuzhambu.common.knowledge.model.health.KnowledgeHealthResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemDeleteRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemListRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemPageResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemUpsertRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

public class FastGptKnowledgeBaseClient implements KnowledgeBaseClient {

    private static final String PROVIDER = "fastgpt";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;
    private final KuzhambuKnowledgeProperties.FastGpt properties;

    public FastGptKnowledgeBaseClient(
            RestOperations restOperations, ObjectMapper objectMapper, KuzhambuKnowledgeProperties.FastGpt properties) {
        this.restOperations = restOperations;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public KnowledgeHealthResult health() {
        JsonNode body = restOperations
                .getForEntity("/api/support/openapi/health?apiKey={apiKey}", JsonNode.class, properties.getApiKey())
                .getBody();
        JsonNode data = dataNode(body);
        boolean available = booleanValue(data, "valid") || booleanValue(data, "available");
        String message = textValue(data, "message", available ? "ok" : "unavailable");
        return new KnowledgeHealthResult(available, PROVIDER, message, rawMap(body));
    }

    @Override
    public KnowledgeBasePageResult listKnowledgeBases(KnowledgeBaseListRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (request != null) {
            putIfNotNull(payload, "pageNum", request.pageNum());
            putIfNotNull(payload, "pageSize", request.pageSize());
            putIfHasText(payload, "searchKey", request.searchKey());
        }
        JsonNode body = post("/api/core/dataset/list", payload);
        JsonNode listNode = listNode(dataNode(body));
        List<KnowledgeBaseResult> knowledgeBases = new ArrayList<>();
        if (listNode.isArray()) {
            for (JsonNode item : listNode) {
                knowledgeBases.add(new KnowledgeBaseResult(
                        textValue(item, "datasetId", textValue(item, "_id", textValue(item, "id", null))),
                        textValue(item, "name", null),
                        rawMap(item)));
            }
        }
        return new KnowledgeBasePageResult(knowledgeBases, rawMap(body));
    }

    @Override
    public KnowledgeBaseResult ensureKnowledgeBase(KnowledgeBaseEnsureRequest request) {
        Assert.notNull(request, "Knowledge base ensure request must not be null");
        return new KnowledgeBaseResult(
                configuredKnowledgeBaseId(), request.name(), Map.of("managedBy", "external-fastgpt"));
    }

    @Override
    public KnowledgeItemPageResult listKnowledgeItems(KnowledgeItemListRequest request) {
        Assert.notNull(request, "Knowledge item list request must not be null");
        String knowledgeBaseId = configuredKnowledgeBaseId();
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "datasetId", knowledgeBaseId);
        putIfNotNull(payload, "pageNum", request.pageNum());
        putIfNotNull(payload, "pageSize", request.pageSize());
        putIfHasText(payload, "searchText", request.searchText());
        JsonNode body = post("/api/core/dataset/collection/list", payload);
        JsonNode listNode = listNode(dataNode(body));
        List<KnowledgeItemResult> knowledgeItems = new ArrayList<>();
        if (listNode.isArray()) {
            for (JsonNode item : listNode) {
                knowledgeItems.add(new KnowledgeItemResult(
                        textValue(item, "collectionId", textValue(item, "_id", textValue(item, "id", null))),
                        textValue(item, "datasetId", knowledgeBaseId),
                        textValue(item.path("metadata"), "sourceId", null),
                        textValue(item, "name", null),
                        rawMap(item)));
            }
        }
        return new KnowledgeItemPageResult(knowledgeItems, rawMap(body));
    }

    @Override
    public KnowledgeItemResult upsertKnowledgeItem(KnowledgeItemUpsertRequest request) {
        Assert.notNull(request, "Knowledge item upsert request must not be null");
        String knowledgeBaseId = configuredKnowledgeBaseId();
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "datasetId", knowledgeBaseId);
        putIfHasText(payload, "name", request.title());
        putIfHasText(payload, "text", request.text());
        putIfNotNull(payload, "metadata", request.metadata());
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/core/dataset/collection/create/text", payload);
        JsonNode data = dataNode(body);
        return new KnowledgeItemResult(
                textValue(data, "collectionId", textValue(data, "_id", textValue(data, "id", null))),
                textValue(data, "datasetId", knowledgeBaseId),
                request.itemKey(),
                textValue(data, "name", request.title()),
                rawMap(body));
    }

    @Override
    public KnowledgeSyncResult syncKnowledgeItem(KnowledgeSyncRequest request) {
        Assert.notNull(request, "Knowledge sync request must not be null");
        String knowledgeBaseId = configuredKnowledgeBaseId();
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "datasetId", knowledgeBaseId);
        putIfHasText(payload, "collectionId", request.knowledgeItemId());
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/core/dataset/collection/sync", payload);
        JsonNode data = dataNode(body);
        return new KnowledgeSyncResult(
                textValue(data, "syncId", textValue(data, "id", null)), textValue(data, "status", null), rawMap(body));
    }

    @Override
    public KnowledgeSyncResult deleteKnowledgeItem(KnowledgeItemDeleteRequest request) {
        Assert.notNull(request, "Knowledge item delete request must not be null");
        String knowledgeBaseId = configuredKnowledgeBaseId();
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "datasetId", knowledgeBaseId);
        putIfHasText(payload, "collectionId", request.knowledgeItemId());
        putIfHasText(payload, "sourceId", request.itemKey());
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/core/dataset/collection/delete", payload);
        JsonNode data = dataNode(body);
        return new KnowledgeSyncResult(
                textValue(data, "syncId", textValue(data, "id", null)),
                textValue(data, "status", "DELETED"),
                rawMap(body));
    }

    @Override
    public KnowledgeChatResult chat(KnowledgeChatRequest request) {
        Assert.notNull(request, "Knowledge chat request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "appId", properties.getAppId());
        putIfHasText(payload, "chatId", textOption(request.metadata(), "chatId"));
        putIfHasText(payload, "model", request.model());
        payload.put("stream", request.stream());
        payload.put("messages", chatMessages(request.messages()));
        putIfNotNull(payload, "metadata", request.metadata());
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/v1/chat/completions", payload, chatHeaders());
        String content = extractChatContent(body);
        String id = textValue(body, "id", textOption(request.metadata(), "chatId"));
        String model = textValue(body, "model", request.model());
        return new KnowledgeChatResult(
                id,
                textValue(body, "object", "chat.completion"),
                longValue(body, "created", null),
                model,
                List.of(new KnowledgeChatChoice(0, new KnowledgeChatMessage("assistant", content), "stop")),
                null,
                Collections.emptyList(),
                rawMap(body));
    }

    private JsonNode post(String path, Map<String, Object> payload) {
        return post(path, payload, headers());
    }

    private JsonNode post(String path, Map<String, Object> payload, HttpHeaders headers) {
        String response = restOperations
                .exchange(path, HttpMethod.POST, new HttpEntity<>(writeJson(payload), headers), String.class)
                .getBody();
        return readJson(response);
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize FastGPT request payload", ex);
        }
    }

    private JsonNode readJson(String response) {
        if (!StringUtils.hasText(response)) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to parse FastGPT response payload", ex);
        }
    }

    private String configuredKnowledgeBaseId() {
        if (StringUtils.hasText(properties.getKnowledgeBaseId())) {
            return properties.getKnowledgeBaseId();
        }
        throw new IllegalStateException("Missing FastGPT knowledge base id configuration");
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private HttpHeaders chatHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(resolveChatApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private String resolveChatApiKey() {
        if (StringUtils.hasText(properties.getChatApiKey())) {
            return properties.getChatApiKey();
        }
        if (StringUtils.hasText(properties.getApiKey()) && StringUtils.hasText(properties.getAppId())) {
            return properties.getApiKey() + "-" + properties.getAppId();
        }
        return properties.getApiKey();
    }

    private List<Map<String, Object>> chatMessages(List<KnowledgeChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (KnowledgeChatMessage message : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            putIfHasText(item, "role", message.role());
            putIfHasText(item, "content", message.content());
            result.add(item);
        }
        return result;
    }

    private String extractChatContent(JsonNode body) {
        JsonNode choices = body == null ? null : body.path("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            String content = textValue(choices.get(0).path("message"), "content", null);
            if (StringUtils.hasText(content)) {
                return content;
            }
        }
        JsonNode data = dataNode(body);
        return textValue(data, "answer", textValue(data, "content", null));
    }

    private JsonNode dataNode(JsonNode body) {
        if (body == null || body.isMissingNode() || body.isNull()) {
            return objectMapper.nullNode();
        }
        JsonNode data = body.path("data");
        return data.isMissingNode() || data.isNull() ? body : data;
    }

    private JsonNode listNode(JsonNode data) {
        JsonNode list = data.path("list");
        if (!list.isMissingNode()) {
            return list;
        }
        JsonNode records = data.path("records");
        if (!records.isMissingNode()) {
            return records;
        }
        return data.isArray() ? data : objectMapper.createArrayNode();
    }

    private Map<String, Object> rawMap(JsonNode body) {
        if (body == null || body.isMissingNode() || body.isNull()) {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(body, MAP_TYPE);
    }

    private void mergeOptions(Map<String, Object> payload, Map<String, Object> options) {
        if (options != null) {
            payload.putAll(options);
        }
    }

    private void putIfHasText(Map<String, Object> payload, String key, String value) {
        if (StringUtils.hasText(value)) {
            payload.put(key, value);
        }
    }

    private void putIfNotNull(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }

    private boolean booleanValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return !value.isMissingNode() && value.asBoolean(false);
    }

    private String textValue(JsonNode node, String fieldName, String fallback) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }
        return value.asText(fallback);
    }

    private String idValue(JsonNode node, String... fieldNames) {
        if (node != null && node.isTextual()) {
            return node.asText();
        }
        for (String fieldName : fieldNames) {
            String value = textValue(node, fieldName, null);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private Long longValue(JsonNode node, String fieldName, Long fallback) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }
        return value.asLong();
    }

    private String textOption(Map<String, Object> options, String key) {
        if (options == null || !options.containsKey(key) || options.get(key) == null) {
            return null;
        }
        return options.get(key).toString();
    }
}
