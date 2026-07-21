package com.thundax.kuzhambu.common.knowledge.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.knowledge.configure.KuzhambuKnowledgeProperties;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseEnsureRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseListRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatMessage;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class FastGptKnowledgeBaseClientTest {

    @Test
    public void shouldListKnowledgeBasesWithAuthorizationHeader() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://fastgpt.local/api/core/dataset/list"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer fastgpt-test"))
                .andRespond(withSuccess(
                        """
                        {"data":{"list":[{"_id":"dataset-1","name":"Discovery QA"}]}}
                        """,
                        MediaType.APPLICATION_JSON));

        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertEquals(
                "dataset-1",
                client.listKnowledgeBases(new KnowledgeBaseListRequest(1, 20, "Discovery"))
                        .knowledgeBases()
                        .get(0)
                        .knowledgeBaseId());
        server.verify();
    }

    @Test
    public void shouldExtractOpenAiCompatibleChatContent() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://fastgpt.local/api/v1/chat/completions"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer fastgpt-test-app-1"))
                .andRespond(withSuccess(
                        """
                        {"choices":[{"message":{"content":"answer from fastgpt"}}]}
                        """,
                        MediaType.APPLICATION_JSON));

        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertEquals(
                "answer from fastgpt",
                client.chat(new KnowledgeChatRequest(
                                "kuzhambu-qa",
                                List.of(new KnowledgeChatMessage("user", "question")),
                                false,
                                Map.of("chatId", "chat-1", "knowledgeBases", List.of("WANGQI_DOCUMENT")),
                                null))
                        .choices()
                        .get(0)
                        .message()
                        .content());
        server.verify();
    }

    @Test
    public void shouldStreamOpenAiCompatibleChatDeltas() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://fastgpt.local/api/v1/chat/completions"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer fastgpt-test-app-1"))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(
                        content()
                                .json(
                                        """
                                {
                                  "appId": "app-1",
                                  "chatId": "chat-1",
                                  "model": "kuzhambu-qa",
                                  "stream": true,
                                  "messages": [{"role":"user","content":"question"}]
                                }
                                """))
                .andRespond(withSuccess(
                        """
                        data: {"choices":[{"delta":{"content":"礼学"},"index":0,"finish_reason":null}]}

                        data: {"choices":[{"delta":{"content":"是礼制之学"},"index":0,"finish_reason":null}]}

                        data: {"choices":[{"delta":{},"index":0,"finish_reason":"stop"}]}

                        """,
                        MediaType.TEXT_EVENT_STREAM));

        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));
        List<String> deltas = new java.util.ArrayList<>();

        assertEquals(
                "礼学是礼制之学",
                client.chatStream(
                                new KnowledgeChatRequest(
                                        "kuzhambu-qa",
                                        List.of(new KnowledgeChatMessage("user", "question")),
                                        true,
                                        Map.of("chatId", "chat-1"),
                                        null),
                                deltas::add)
                        .choices()
                        .get(0)
                        .message()
                        .content());
        assertEquals(List.of("礼学", "是礼制之学"), deltas);
        server.verify();
    }

    @Test
    public void shouldPreferConfiguredChatApiKeyForChatRequests() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://fastgpt.local/api/v1/chat/completions"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer fastgpt-chat-test"))
                .andRespond(withSuccess(
                        """
                        {"choices":[{"message":{"content":"answer from app key"}}]}
                        """,
                        MediaType.APPLICATION_JSON));

        KuzhambuKnowledgeProperties.FastGpt properties = fastGptProperties("http://fastgpt.local", "fastgpt-test");
        properties.setChatApiKey("fastgpt-chat-test");
        FastGptKnowledgeBaseClient client =
                new FastGptKnowledgeBaseClient(restTemplate, new ObjectMapper(), properties);

        assertEquals(
                "answer from app key",
                client.chat(new KnowledgeChatRequest(
                                "kuzhambu-qa",
                                List.of(new KnowledgeChatMessage("user", "question")),
                                false,
                                null,
                                null))
                        .choices()
                        .get(0)
                        .message()
                        .content());
        server.verify();
    }

    @Test
    public void shouldUpsertKnowledgeItemWithExistingKnowledgeBaseIdWithoutCreatingDataset() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://fastgpt.local/api/core/dataset/collection/create/text"))
                .andExpect(
                        content()
                                .json(
                                        """
                                {
                                  "datasetId": "6a4f51e5ef72393d430a8e31",
                                  "name": "三才",
                                  "text": "三才指天地人。"
                                }
                                """))
                .andRespond(withSuccess(
                        "{\"code\":200,\"data\":{\"collectionId\":\"item-1\"}}", MediaType.APPLICATION_JSON));

        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertEquals(
                "item-1",
                client.upsertKnowledgeItem(
                                new com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemUpsertRequest(
                                        "kuzhambu-qa",
                                        "SANCAI_ENTRY:3001",
                                        "三才",
                                        "三才指天地人。",
                                        null,
                                        Map.of("knowledgeBaseId", "6a4f51e5ef72393d430a8e31")))
                        .knowledgeItemId());
        server.verify();
    }

    @Test
    public void shouldResolveKnowledgeBaseFromConfigurationWithoutCreatingDataset() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();

        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertEquals(
                "6a4f51e5ef72393d430a8e31",
                client.ensureKnowledgeBase(new KnowledgeBaseEnsureRequest("kuzhambu-qa", "QA", null))
                        .knowledgeBaseId());
        server.verify();
    }

    @Test
    public void shouldRejectReservedOptionKeysForKnowledgeItemUpsert() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertThrows(
                IllegalArgumentException.class,
                () -> client.upsertKnowledgeItem(
                        new com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemUpsertRequest(
                                "kuzhambu-qa",
                                "SANCAI_ENTRY:3001",
                                "三才",
                                "三才指天地人。",
                                null,
                                Map.of("datasetId", "other-dataset"))));
        server.verify();
    }

    @Test
    public void shouldRejectReservedOptionKeysForChat() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertThrows(
                IllegalArgumentException.class,
                () -> client.chat(new KnowledgeChatRequest(
                        "kuzhambu-qa",
                        List.of(new KnowledgeChatMessage("user", "question")),
                        false,
                        null,
                        Map.of("messages", List.of(Map.of("role", "system", "content", "override"))))));
        server.verify();
    }

    @Test
    public void shouldTreatKnowledgeItemSyncAsCompletedWhenSyncModeIsDisabled() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        KuzhambuKnowledgeProperties.FastGpt properties = fastGptProperties("http://fastgpt.local", "fastgpt-test");
        properties.setSyncMode(KuzhambuKnowledgeProperties.FastGpt.SyncMode.DISABLED);
        FastGptKnowledgeBaseClient client =
                new FastGptKnowledgeBaseClient(restTemplate, new ObjectMapper(), properties);

        assertEquals(
                "SUCCEEDED",
                client.syncKnowledgeItem(new KnowledgeSyncRequest("kuzhambu-qa", "item-1", Map.of("trigger", "FULL")))
                        .status());
        server.verify();
    }

    @Test
    public void shouldCallFastGptSyncEndpointWhenAutoDetectsEmbeddingDataset() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://fastgpt.local/api/core/dataset/list"))
                .andRespond(withSuccess(
                        """
                        {"data":[{"_id":"6a4f51e5ef72393d430a8e31","vectorModel":{"type":"embedding","model":"bge-m3"}}]}
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://fastgpt.local/api/core/dataset/collection/sync"))
                .andExpect(
                        content()
                                .json(
                                        """
                                {
                                  "datasetId": "6a4f51e5ef72393d430a8e31",
                                  "collectionId": "item-1",
                                  "trigger": "FULL"
                                }
                                """))
                .andRespond(withSuccess(
                        "{\"code\":200,\"data\":{\"syncId\":\"sync-1\",\"status\":\"RUNNING\"}}",
                        MediaType.APPLICATION_JSON));
        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertEquals(
                "RUNNING",
                client.syncKnowledgeItem(new KnowledgeSyncRequest("kuzhambu-qa", "item-1", Map.of("trigger", "FULL")))
                        .status());
        server.verify();
    }

    @Test
    public void shouldSkipFastGptSyncEndpointWhenAutoDetectsNonEmbeddingDataset() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://fastgpt.local/api/core/dataset/list"))
                .andRespond(withSuccess(
                        """
                        {"data":[{"_id":"6a4f51e5ef72393d430a8e31","vectorModel":null}]}
                        """,
                        MediaType.APPLICATION_JSON));
        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertEquals(
                "SUCCEEDED",
                client.syncKnowledgeItem(new KnowledgeSyncRequest("kuzhambu-qa", "item-1", Map.of("trigger", "FULL")))
                        .status());
        server.verify();
    }

    @Test
    public void shouldTreatNotSupportSyncAsCompletedInAutoMode() {
        RestTemplate restTemplate =
                new RestTemplateBuilder().rootUri("http://fastgpt.local").build();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://fastgpt.local/api/core/dataset/list"))
                .andRespond(withSuccess(
                        """
                        {"data":[{"_id":"6a4f51e5ef72393d430a8e31","vectorModel":{"type":"embedding","model":"bge-m3"}}]}
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://fastgpt.local/api/core/dataset/collection/sync"))
                .andRespond(
                        withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(
                                        """
                                {"code":501001,"statusText":"notSupportSync","message":"common:core.dataset.error.notSupportSync"}
                                """));
        FastGptKnowledgeBaseClient client = new FastGptKnowledgeBaseClient(
                restTemplate, new ObjectMapper(), fastGptProperties("http://fastgpt.local", "fastgpt-test"));

        assertEquals(
                "SUCCEEDED",
                client.syncKnowledgeItem(new KnowledgeSyncRequest("kuzhambu-qa", "item-1", Map.of("trigger", "FULL")))
                        .status());
        server.verify();
    }

    private KuzhambuKnowledgeProperties.FastGpt fastGptProperties(String baseUrl, String apiKey) {
        KuzhambuKnowledgeProperties.FastGpt properties = new KuzhambuKnowledgeProperties.FastGpt();
        properties.setBaseUrl(baseUrl);
        properties.setApiKey(apiKey);
        properties.setAppId("app-1");
        properties.setKnowledgeBaseId("6a4f51e5ef72393d430a8e31");
        properties.setTimeout(Duration.ofSeconds(10));
        return properties;
    }
}
