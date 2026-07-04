package com.thundax.kuzhambu.common.knowledge.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.knowledge.configure.KuzhambuKnowledgeProperties;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseListRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatMessage;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
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
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer fastgpt-test"))
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

    private KuzhambuKnowledgeProperties.FastGpt fastGptProperties(String baseUrl, String apiKey) {
        KuzhambuKnowledgeProperties.FastGpt properties = new KuzhambuKnowledgeProperties.FastGpt();
        properties.setBaseUrl(baseUrl);
        properties.setApiKey(apiKey);
        properties.setAppId("app-1");
        properties.setTimeout(Duration.ofSeconds(10));
        return properties;
    }
}
