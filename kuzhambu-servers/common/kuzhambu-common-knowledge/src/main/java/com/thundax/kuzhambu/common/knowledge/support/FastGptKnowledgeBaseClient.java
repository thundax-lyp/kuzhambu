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
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatSource;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatStreamHandler;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatUsage;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionCreateRequest;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionReferenceRequest;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionResult;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionUpdateRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataListRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPageResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushItem;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataReferenceRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataResult;
import com.thundax.kuzhambu.common.knowledge.model.health.KnowledgeHealthResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemDeleteRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemListRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemPageResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemUpsertRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestOperations;

public class FastGptKnowledgeBaseClient implements KnowledgeBaseClient {

    private static final String PROVIDER = "fastgpt";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final Set<String> RESERVED_OPTION_KEYS = Set.of(
            "appId",
            "chatId",
            "collectionId",
            "datasetId",
            "messages",
            "metadata",
            "model",
            "name",
            "sourceId",
            "stream",
            "text");

    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;
    private final KuzhambuKnowledgeProperties.FastGpt properties;
    private final Object syncMonitor = new Object();
    private volatile Boolean supportsSyncCache;

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
        KuzhambuKnowledgeProperties.FastGpt.SyncMode syncMode = properties.getSyncMode();
        if (KuzhambuKnowledgeProperties.FastGpt.SyncMode.DISABLED.equals(syncMode)) {
            return skippedSyncResult(request, "DISABLED");
        }
        if (KuzhambuKnowledgeProperties.FastGpt.SyncMode.AUTO.equals(syncMode) && !supportsFastGptSync()) {
            return skippedSyncResult(request, "NOT_SUPPORTED");
        }
        try {
            synchronized (syncMonitor) {
                return doSyncKnowledgeItem(request);
            }
        } catch (RestClientResponseException ex) {
            if (KuzhambuKnowledgeProperties.FastGpt.SyncMode.AUTO.equals(syncMode) && isNotSupportSync(ex)) {
                supportsSyncCache = false;
                return skippedSyncResult(request, "NOT_SUPPORTED");
            }
            throw ex;
        }
    }

    private KnowledgeSyncResult doSyncKnowledgeItem(KnowledgeSyncRequest request) {
        String knowledgeBaseId = configuredKnowledgeBaseId();
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "datasetId", knowledgeBaseId);
        putIfHasText(payload, "collectionId", request.knowledgeItemId());
        mergeOptions(payload, request.options());
        JsonNode body = post("/api/core/dataset/collection/sync", payload);
        JsonNode data = dataNode(body);
        return new KnowledgeSyncResult(
                textValue(data, "syncId", textValue(data, "id", request.knowledgeItemId())),
                textValue(data, "status", "SUCCEEDED"),
                rawMap(body));
    }

    private KnowledgeSyncResult skippedSyncResult(KnowledgeSyncRequest request, String reason) {
        Map<String, Object> raw = new LinkedHashMap<>();
        putIfHasText(raw, "provider", PROVIDER);
        putIfHasText(raw, "knowledgeItemId", request.knowledgeItemId());
        putIfHasText(raw, "syncMode", properties.getSyncMode().name());
        putIfHasText(raw, "skipReason", reason);
        putIfNotNull(raw, "options", request.options());
        return new KnowledgeSyncResult(request.knowledgeItemId(), "SUCCEEDED", raw);
    }

    @Override
    public KnowledgeSyncResult deleteKnowledgeItem(KnowledgeItemDeleteRequest request) {
        Assert.notNull(request, "Knowledge item delete request must not be null");
        deleteCollection(new KnowledgeCollectionReferenceRequest(request.knowledgeItemId()));
        return new KnowledgeSyncResult(request.knowledgeItemId(), "DELETED", Map.of("provider", PROVIDER));
    }

    @Override
    public KnowledgeCollectionResult createCollection(KnowledgeCollectionCreateRequest request) {
        Assert.notNull(request, "Knowledge collection create request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(
                payload,
                "datasetId",
                StringUtils.hasText(request.datasetId()) ? request.datasetId() : configuredKnowledgeBaseId());
        putIfHasText(payload, "name", request.name());
        putIfHasText(payload, "type", request.type());
        JsonNode body = requireSuccess(post("/api/core/dataset/collection/create", payload));
        JsonNode data = dataNode(body);
        String collectionId = data.isTextual()
                ? data.asText()
                : textValue(data, "_id", textValue(data, "collectionId", textValue(data, "id", null)));
        return new KnowledgeCollectionResult(collectionId, booleanValue(data, "forbid"), rawMap(body));
    }

    @Override
    public KnowledgeCollectionResult getCollection(KnowledgeCollectionReferenceRequest request) {
        Assert.notNull(request, "Knowledge collection reference request must not be null");
        try {
            JsonNode body = get("/api/core/dataset/collection/detail?id={id}", Map.of("id", request.collectionId()));
            if (isProviderNotFound(body)) {
                return null;
            }
            body = requireSuccess(body);
            JsonNode data = dataNode(body);
            return new KnowledgeCollectionResult(
                    textValue(data, "_id", textValue(data, "collectionId", textValue(data, "id", null))),
                    booleanValue(data, "forbid"),
                    rawMap(body));
        } catch (RestClientResponseException ex) {
            if (isNotFound(ex)) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public void updateCollection(KnowledgeCollectionUpdateRequest request) {
        Assert.notNull(request, "Knowledge collection update request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "id", request.collectionId());
        payload.put("forbid", request.forbid());
        requireSuccess(post("/api/core/dataset/collection/update", payload));
    }

    @Override
    public void deleteCollection(KnowledgeCollectionReferenceRequest request) {
        Assert.notNull(request, "Knowledge collection reference request must not be null");
        try {
            JsonNode body = delete("/api/core/dataset/collection/delete?id={id}", Map.of("id", request.collectionId()));
            if (!isProviderNotFound(body)) {
                requireSuccess(body);
            }
        } catch (RestClientResponseException ex) {
            if (!isNotFound(ex)) {
                throw ex;
            }
        }
    }

    @Override
    public KnowledgeCollectionDataPageResult listCollectionData(KnowledgeCollectionDataListRequest request) {
        Assert.notNull(request, "Knowledge collection data list request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "collectionId", request.collectionId());
        putIfNotNull(payload, "offset", request.offset());
        putIfNotNull(payload, "pageSize", request.pageSize());
        JsonNode body = requireSuccess(post("/api/core/dataset/data/v2/list", payload));
        JsonNode data = dataNode(body);
        List<KnowledgeCollectionDataResult> items = new ArrayList<>();
        JsonNode list = listNode(data);
        if (list.isArray()) {
            for (JsonNode item : list) {
                items.add(new KnowledgeCollectionDataResult(textValue(item, "_id", textValue(item, "id", null))));
            }
        }
        return new KnowledgeCollectionDataPageResult(intValue(data, "total", items.size()), items, rawMap(body));
    }

    @Override
    public void deleteCollectionData(KnowledgeCollectionDataReferenceRequest request) {
        Assert.notNull(request, "Knowledge collection data reference request must not be null");
        try {
            JsonNode body = delete("/api/core/dataset/data/delete?id={id}", Map.of("id", request.dataId()));
            if (!isProviderNotFound(body)) {
                requireSuccess(body);
            }
        } catch (RestClientResponseException ex) {
            if (!isNotFound(ex)) {
                throw ex;
            }
        }
    }

    @Override
    public KnowledgeCollectionDataPushResult pushCollectionData(KnowledgeCollectionDataPushRequest request) {
        Assert.notNull(request, "Knowledge collection data push request must not be null");
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "collectionId", request.collectionId());
        List<Map<String, Object>> data = new ArrayList<>();
        if (request.data() != null) {
            for (KnowledgeCollectionDataPushItem item : request.data()) {
                Map<String, Object> value = new LinkedHashMap<>();
                putIfHasText(value, "q", item.q());
                putIfHasText(value, "a", item.a());
                value.put("chunkIndex", item.chunkIndex());
                data.add(value);
            }
        }
        payload.put("data", data);
        JsonNode body = requireSuccess(post("/api/core/dataset/data/pushData", payload));
        JsonNode result = dataNode(body);
        return new KnowledgeCollectionDataPushResult(
                intValue(result, "insertLen", intValue(body, "insertLen", 0)), rawMap(body));
    }

    @Override
    public KnowledgeChatResult chat(KnowledgeChatRequest request) {
        Assert.notNull(request, "Knowledge chat request must not be null");
        Map<String, Object> payload = chatPayload(request, request.stream());
        JsonNode body = post("/api/v1/chat/completions", payload, chatHeaders());
        return chatResult(body, textOption(request.metadata(), "chatId"), request.model(), null, false);
    }

    @Override
    public KnowledgeChatResult chatStream(KnowledgeChatRequest request, KnowledgeChatStreamHandler streamHandler) {
        Assert.notNull(request, "Knowledge chat request must not be null");
        Assert.notNull(streamHandler, "Knowledge chat stream handler must not be null");
        Map<String, Object> payload = chatPayload(request, true);
        ChatStreamResult streamResult = restOperations.execute(
                "/api/v1/chat/completions",
                HttpMethod.POST,
                clientRequest -> {
                    clientRequest.getHeaders().putAll(chatStreamHeaders());
                    byte[] body = writeJson(payload).getBytes(StandardCharsets.UTF_8);
                    clientRequest.getHeaders().setContentLength(body.length);
                    clientRequest.getBody().write(body);
                },
                response -> readChatStream(response, streamHandler));
        return chatResult(
                streamResult.finalPayload(),
                textOption(request.metadata(), "chatId"),
                request.model(),
                streamResult.content(),
                true);
    }

    private Map<String, Object> chatPayload(KnowledgeChatRequest request, boolean stream) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "appId", properties.getAppId());
        putIfHasText(payload, "chatId", textOption(request.metadata(), "chatId"));
        putIfHasText(payload, "model", request.model());
        payload.put("stream", stream);
        payload.put("messages", chatMessages(request.messages()));
        putIfNotNull(payload, "metadata", request.metadata());
        mergeOptions(payload, request.options());
        return payload;
    }

    private ChatStreamResult readChatStream(ClientHttpResponse response, KnowledgeChatStreamHandler streamHandler)
            throws IOException {
        ChatStreamAccumulator accumulator = new ChatStreamAccumulator();
        StringBuilder eventBlock = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    if (appendChatStreamBlock(eventBlock.toString(), accumulator, streamHandler)) {
                        return accumulator.toResult();
                    }
                    eventBlock.setLength(0);
                    continue;
                }
                eventBlock.append(line).append('\n');
            }
        }
        appendChatStreamBlock(eventBlock.toString(), accumulator, streamHandler);
        return accumulator.toResult();
    }

    private boolean appendChatStreamBlock(
            String eventBlock, ChatStreamAccumulator accumulator, KnowledgeChatStreamHandler streamHandler) {
        if (!StringUtils.hasText(eventBlock)) {
            return false;
        }
        String eventName = null;
        for (String line : eventBlock.split("\\R")) {
            if (line.startsWith("event:")) {
                eventName = line.substring("event:".length()).trim();
                continue;
            }
            if (!line.startsWith("data:")) {
                continue;
            }
            String data = line.substring("data:".length()).trim();
            if (!StringUtils.hasText(data)) {
                continue;
            }
            if ("[DONE]".equals(data)) {
                accumulator.markDone();
                return true;
            }
            JsonNode body = readChatStreamPayload(data);
            accumulator.remember(body);
            boolean finalEvent = isFinalChatStreamEvent(eventName);
            String delta = finalEvent && accumulator.hasContent() ? null : extractChatStreamDelta(body);
            if (StringUtils.hasText(delta)) {
                accumulator.append(delta);
                streamHandler.onDelta(delta);
            }
            if (isChatStreamFinished(body)) {
                accumulator.markFinal(body);
            }
            if (finalEvent) {
                accumulator.markFinal(body);
                return true;
            }
        }
        return false;
    }

    private boolean isFinalChatStreamEvent(String eventName) {
        return "final".equalsIgnoreCase(eventName)
                || "completed".equalsIgnoreCase(eventName)
                || "completion".equalsIgnoreCase(eventName);
    }

    private boolean isChatStreamFinished(JsonNode body) {
        JsonNode choices = body.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return false;
        }
        JsonNode choice = choices.get(0);
        return StringUtils.hasText(textValue(choice, "finish_reason", textValue(choice, "finishReason", null)));
    }

    private JsonNode readChatStreamPayload(String data) {
        try {
            return objectMapper.readTree(data);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to parse FastGPT stream payload", ex);
        }
    }

    private String extractChatStreamDelta(JsonNode body) {
        JsonNode choices = body.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode choice = choices.get(0);
            String delta = textValue(choice.path("delta"), "content", null);
            if (StringUtils.hasText(delta)) {
                return delta;
            }
            return textValue(choice.path("message"), "content", null);
        }
        JsonNode dataNode = dataNode(body);
        return textValue(dataNode, "content", textValue(dataNode, "answer", null));
    }

    private KnowledgeChatResult chatResult(
            JsonNode body, String fallbackId, String fallbackModel, String contentOverride, boolean stream) {
        JsonNode resultBody = body == null ? objectMapper.createObjectNode() : body;
        JsonNode resultData = dataNode(resultBody);
        String content = contentOverride == null ? extractChatContent(resultBody) : contentOverride;
        String id = textValue(resultBody, "id", textValue(resultData, "id", fallbackId));
        String model = textValue(resultBody, "model", textValue(resultData, "model", fallbackModel));
        Map<String, Object> raw = new LinkedHashMap<>(rawMap(resultBody));
        raw.putIfAbsent("provider", PROVIDER);
        raw.putIfAbsent("stream", stream);
        return new KnowledgeChatResult(
                id,
                textValue(resultBody, "object", textValue(resultData, "object", "chat.completion")),
                longValue(resultBody, "created", longValue(resultData, "created", null)),
                model,
                List.of(new KnowledgeChatChoice(
                        0, new KnowledgeChatMessage("assistant", content), finishReason(resultBody))),
                chatUsage(resultBody),
                chatSources(resultBody),
                raw);
    }

    private String finishReason(JsonNode body) {
        JsonNode choices = body.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            JsonNode choice = choices.get(0);
            return textValue(choice, "finish_reason", textValue(choice, "finishReason", "stop"));
        }
        return "stop";
    }

    private KnowledgeChatUsage chatUsage(JsonNode body) {
        JsonNode usage = firstPresent(
                body.path("usage"),
                dataNode(body).path("usage"),
                responseDataNode(body).path("usage"));
        if (usage.isMissingNode() || usage.isNull()) {
            return null;
        }
        Integer promptTokens = intValue(usage, "promptTokens", intValue(usage, "prompt_tokens", null));
        Integer completionTokens = intValue(usage, "completionTokens", intValue(usage, "completion_tokens", null));
        Integer totalTokens = intValue(usage, "totalTokens", intValue(usage, "total_tokens", null));
        if (promptTokens == null && completionTokens == null && totalTokens == null) {
            return null;
        }
        return new KnowledgeChatUsage(promptTokens, completionTokens, totalTokens);
    }

    private List<KnowledgeChatSource> chatSources(JsonNode body) {
        JsonNode sources = firstPresent(
                body.path("sources"),
                dataNode(body).path("sources"),
                responseDataNode(body).path("sources"),
                body.path("quoteList"),
                dataNode(body).path("quoteList"),
                responseDataNode(body).path("quoteList"));
        if (!sources.isArray() || sources.isEmpty()) {
            return Collections.emptyList();
        }
        List<KnowledgeChatSource> results = new ArrayList<>();
        for (JsonNode source : sources) {
            JsonNode metadata = source.path("metadata");
            results.add(new KnowledgeChatSource(
                    textValue(
                            source, "sourceId", textValue(source, "source_id", textValue(metadata, "sourceId", null))),
                    textValue(
                            source,
                            "knowledgeBase",
                            textValue(source, "datasetName", textValue(source, "datasetId", null))),
                    textValue(
                            source,
                            "contentType",
                            textValue(source, "sourceType", textValue(metadata, "contentType", null))),
                    textValue(source, "contentId", textValue(metadata, "contentId", null)),
                    textValue(source, "title", textValue(source, "name", textValue(source, "sourceName", null))),
                    textValue(source, "snippet", textValue(source, "content", textValue(source, "text", null))),
                    doubleValue(source, "score", doubleValue(source, "similarity", null)),
                    rawMap(source)));
        }
        return results;
    }

    private JsonNode responseDataNode(JsonNode body) {
        JsonNode responseData = body.path("responseData");
        if (!responseData.isMissingNode() && !responseData.isNull()) {
            return responseData;
        }
        return dataNode(body).path("responseData");
    }

    private JsonNode firstPresent(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                return node;
            }
        }
        return objectMapper.getNodeFactory().missingNode();
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

    private JsonNode get(String path, Map<String, ?> uriVariables) {
        String response = restOperations
                .exchange(path, HttpMethod.GET, new HttpEntity<>(headers()), String.class, uriVariables)
                .getBody();
        return readJson(response);
    }

    private JsonNode delete(String path, Map<String, ?> uriVariables) {
        String response = restOperations
                .exchange(path, HttpMethod.DELETE, new HttpEntity<>(headers()), String.class, uriVariables)
                .getBody();
        return readJson(response);
    }

    private JsonNode requireSuccess(JsonNode body) {
        Integer code = intValue(body, "code", null);
        if (code == null || code == 0 || code == 200) {
            return body;
        }
        throw new IllegalStateException("FastGPT request failed with provider code " + code);
    }

    private boolean isProviderNotFound(JsonNode body) {
        if (body == null) {
            return false;
        }
        Integer code = intValue(body, "code", null);
        String message = textValue(body, "message", textValue(body, "statusText", ""));
        return (code != null && code == 404)
                || (StringUtils.hasText(message)
                        && (message.toLowerCase(java.util.Locale.ROOT).contains("not found")
                                || message.toLowerCase(java.util.Locale.ROOT).contains("notfound")));
    }

    private boolean isNotFound(RestClientResponseException exception) {
        return exception.getStatusCode().value() == 404;
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

    private HttpHeaders chatStreamHeaders() {
        HttpHeaders headers = chatHeaders();
        headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
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
        JsonNode nestedData = data.path("data");
        if (nestedData.isArray()) {
            return nestedData;
        }
        return data.isArray() ? data : objectMapper.createArrayNode();
    }

    private boolean supportsFastGptSync() {
        Boolean cachedValue = supportsSyncCache;
        if (cachedValue != null) {
            return cachedValue;
        }
        Boolean detectedValue = detectSupportsFastGptSync();
        if (detectedValue != null) {
            supportsSyncCache = detectedValue;
            return detectedValue;
        }
        return true;
    }

    private Boolean detectSupportsFastGptSync() {
        String knowledgeBaseId = configuredKnowledgeBaseId();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pageNum", 1);
        payload.put("pageSize", 100);
        JsonNode body;
        try {
            body = post("/api/core/dataset/list", payload);
        } catch (RestClientException ex) {
            return null;
        }
        JsonNode listNode = listNode(dataNode(body));
        if (!listNode.isArray()) {
            return null;
        }
        for (JsonNode item : listNode) {
            String itemId = textValue(item, "_id", textValue(item, "datasetId", textValue(item, "id", null)));
            if (knowledgeBaseId.equals(itemId)) {
                return hasEmbeddingVectorModel(item);
            }
        }
        return null;
    }

    private boolean hasEmbeddingVectorModel(JsonNode dataset) {
        JsonNode vectorModel = dataset.path("vectorModel");
        if (vectorModel.isMissingNode() || vectorModel.isNull()) {
            return false;
        }
        if (vectorModel.isTextual()) {
            return StringUtils.hasText(vectorModel.asText());
        }
        return StringUtils.hasText(textValue(vectorModel, "model", null))
                || "embedding".equalsIgnoreCase(textValue(vectorModel, "type", null));
    }

    private boolean isNotSupportSync(RestClientResponseException ex) {
        JsonNode body;
        try {
            body = readJson(ex.getResponseBodyAsString());
        } catch (RuntimeException parseException) {
            return false;
        }
        return body.path("code").asInt() == 501001
                || "notSupportSync".equalsIgnoreCase(textValue(body, "statusText", null));
    }

    private Map<String, Object> rawMap(JsonNode body) {
        if (body == null || body.isMissingNode() || body.isNull()) {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(body, MAP_TYPE);
    }

    private void mergeOptions(Map<String, Object> payload, Map<String, Object> options) {
        if (options != null) {
            Set<String> reservedKeys = new HashSet<>(options.keySet());
            reservedKeys.retainAll(RESERVED_OPTION_KEYS);
            if (!reservedKeys.isEmpty()) {
                throw new IllegalArgumentException("FastGPT options contain reserved keys: " + reservedKeys);
            }
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

    private Integer intValue(JsonNode node, String fieldName, Integer fallback) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }
        return value.asInt();
    }

    private Double doubleValue(JsonNode node, String fieldName, Double fallback) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }
        return value.asDouble();
    }

    private String textOption(Map<String, Object> options, String key) {
        if (options == null || !options.containsKey(key) || options.get(key) == null) {
            return null;
        }
        return options.get(key).toString();
    }

    private static final class ChatStreamAccumulator {

        private final StringBuilder content = new StringBuilder();
        private JsonNode lastPayload;
        private JsonNode finalPayload;

        private void append(String delta) {
            content.append(delta);
        }

        private void remember(JsonNode body) {
            lastPayload = body;
        }

        private void markFinal(JsonNode body) {
            finalPayload = body;
        }

        private void markDone() {
            if (finalPayload == null) {
                finalPayload = lastPayload;
            }
        }

        private boolean hasContent() {
            return !content.isEmpty();
        }

        private ChatStreamResult toResult() {
            return new ChatStreamResult(content.toString(), finalPayload == null ? lastPayload : finalPayload);
        }
    }

    private record ChatStreamResult(String content, JsonNode finalPayload) {}
}
