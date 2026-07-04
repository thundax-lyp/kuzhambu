package com.thundax.kuzhambu.common.knowledge.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.configure.KuzhambuKnowledgeProperties;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeChatMessage;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeChatResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeCollectionCreateRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeCollectionListRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeCollectionPageResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeCollectionResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeDatasetCreateRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeDatasetListRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeDatasetPageResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeDatasetResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeHealthResult;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeSyncRequest;
import com.thundax.kuzhambu.common.knowledge.model.KnowledgeSyncResult;
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
    public KnowledgeDatasetPageResult listDatasets(KnowledgeDatasetListRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (request != null) {
            putIfNotNull(payload, "pageNum", request.pageNum());
            putIfNotNull(payload, "pageSize", request.pageSize());
            putIfHasText(payload, "searchKey", request.searchKey());
        }
        JsonNode body = post("/api/core/dataset/list", payload);
        JsonNode listNode = listNode(dataNode(body));
        List<KnowledgeDatasetResult> datasets = new ArrayList<>();
        if (listNode.isArray()) {
            for (JsonNode item : listNode) {
                datasets.add(new KnowledgeDatasetResult(
                        textValue(item, "datasetId", textValue(item, "_id", textValue(item, "id", null))),
                        textValue(item, "name", null),
                        rawMap(item)));
            }
        }
        return new KnowledgeDatasetPageResult(datasets, rawMap(body));
    }

    @Override
    public KnowledgeDatasetResult createDataset(KnowledgeDatasetCreateRequest request) {
        Assert.notNull(request, "Knowledge dataset create request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "dataset");
        putIfHasText(payload, "name", request.name());
        putIfHasText(payload, "intro", request.description());
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/core/dataset/create", payload);
        JsonNode data = dataNode(body);
        return new KnowledgeDatasetResult(
                textValue(data, "datasetId", textValue(data, "_id", textValue(data, "id", null))),
                textValue(data, "name", request.name()),
                rawMap(body));
    }

    @Override
    public KnowledgeCollectionPageResult listCollections(KnowledgeCollectionListRequest request) {
        Assert.notNull(request, "Knowledge collection list request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "datasetId", request.datasetId());
        putIfNotNull(payload, "pageNum", request.pageNum());
        putIfNotNull(payload, "pageSize", request.pageSize());
        putIfHasText(payload, "searchText", request.searchText());
        JsonNode body = post("/api/core/dataset/collection/list", payload);
        JsonNode listNode = listNode(dataNode(body));
        List<KnowledgeCollectionResult> collections = new ArrayList<>();
        if (listNode.isArray()) {
            for (JsonNode item : listNode) {
                collections.add(new KnowledgeCollectionResult(
                        textValue(item, "collectionId", textValue(item, "_id", textValue(item, "id", null))),
                        textValue(item, "datasetId", request.datasetId()),
                        textValue(item, "name", null),
                        rawMap(item)));
            }
        }
        return new KnowledgeCollectionPageResult(collections, rawMap(body));
    }

    @Override
    public KnowledgeCollectionResult createCollection(KnowledgeCollectionCreateRequest request) {
        Assert.notNull(request, "Knowledge collection create request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "datasetId", request.datasetId());
        putIfHasText(payload, "name", request.name());
        putIfHasText(payload, "text", request.text());
        putIfNotNull(payload, "metadata", request.metadata());
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/core/dataset/collection/create/text", payload);
        JsonNode data = dataNode(body);
        return new KnowledgeCollectionResult(
                textValue(data, "collectionId", textValue(data, "_id", textValue(data, "id", null))),
                textValue(data, "datasetId", request.datasetId()),
                textValue(data, "name", request.name()),
                rawMap(body));
    }

    @Override
    public KnowledgeSyncResult syncCollection(KnowledgeSyncRequest request) {
        Assert.notNull(request, "Knowledge sync request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "datasetId", request.datasetId());
        putIfHasText(payload, "collectionId", request.collectionId());
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/core/dataset/collection/sync", payload);
        JsonNode data = dataNode(body);
        return new KnowledgeSyncResult(
                textValue(data, "syncId", textValue(data, "id", null)), textValue(data, "status", null), rawMap(body));
    }

    @Override
    public KnowledgeChatResult chat(KnowledgeChatRequest request) {
        Assert.notNull(request, "Knowledge chat request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "appId", request.appId());
        putIfHasText(payload, "chatId", request.chatId());
        payload.put("stream", request.stream());
        payload.put("messages", chatMessages(request.messages()));
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/v1/chat/completions", payload);
        return new KnowledgeChatResult(request.chatId(), extractChatContent(body), rawMap(body));
    }

    private JsonNode post(String path, Map<String, Object> payload) {
        return restOperations
                .exchange(path, HttpMethod.POST, new HttpEntity<>(payload, headers()), JsonNode.class)
                .getBody();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
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
}
