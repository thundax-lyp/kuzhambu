package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatCompletionChoice;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatCompletionMessage;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatCompletionSource;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatUsageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoveryQaPortalControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepPortalQaApiPaths() throws Exception {
        assertRequestMapping(DiscoveryQaPortalController.class, "/api/portal/discovery/qa");
        assertPostMapping(
                DiscoveryQaPortalController.class,
                "openSession",
                "session/open",
                DiscoveryQaRequests.OpenSessionRequest.class);
        assertPostMapping(
                DiscoveryQaPortalController.class,
                "chatCompletions",
                "chat/completions",
                DiscoveryQaRequests.ChatCompletionsRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        DiscoveryQaRequests.OpenSessionRequest openSessionRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "ownerUserId": 1001,
                  "title": "黄帝问答",
                  "scope": "GLOBAL",
                  "contextMode": "SEARCH",
                  "contextContentType": "SANCAI_ENTRY",
                  "contextContentId": 10001,
                  "requestId": "req-1",
                  "traceId": "trace-1"
                }
                """,
                DiscoveryQaRequests.OpenSessionRequest.class);
        assertEquals(1001L, openSessionRequest.getOwnerUserId());
        assertJsonFields(
                openSessionRequest,
                "ownerUserId",
                "title",
                "scope",
                "contextMode",
                "contextContentType",
                "contextContentId",
                "requestId",
                "traceId");

        DiscoveryQaRequests.ChatCompletionsRequest chatCompletionRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "sessionId": 5001,
                  "model": "kuzhambu-qa",
                  "messages": [
                    {"role": "user", "content": "黄帝是谁"},
                    {"role": "assistant", "content": "上古帝王"}
                  ],
                  "stream": false,
                  "metadata": {"traceId": "trace-1"},
                  "options": {"temperature": 0.2},
                  "requestId": "req-1",
                  "traceId": "trace-1"
                }
                """,
                DiscoveryQaRequests.ChatCompletionsRequest.class);
        assertEquals(5001L, chatCompletionRequest.getSessionId());
        assertJsonFields(chatCompletionRequest, "sessionId", "model", "messages", "requestId", "traceId");
    }

    @Test
    void openSessionShouldDelegateToApplicationService() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalController controller =
                new DiscoveryQaPortalController(service, knowledgeQaApplicationService);
        DiscoveryQaRequests.OpenSessionRequest request = new DiscoveryQaRequests.OpenSessionRequest();
        request.setOwnerUserId(1001L);
        request.setTitle("黄帝问答");
        request.setScope("GLOBAL");
        request.setContextMode("SEARCH");
        request.setContextContentType("SANCAI_ENTRY");
        request.setContextContentId(10001L);
        request.setRequestId("req-1");
        request.setTraceId("trace-1");
        when(service.openSession(any()))
                .thenReturn(new QaSessionResult(
                        9001L,
                        1001L,
                        "黄帝问答",
                        "GLOBAL",
                        "SEARCH",
                        "SANCAI_ENTRY",
                        10001L,
                        "OPEN",
                        1_718_000_000_000L,
                        null));

        var response = controller.openSession(request);

        verify(service)
                .openSession(argThat(command -> command != null
                        && command.getOwnerUserId() != null
                        && command.getOwnerUserId().equals(1001L)
                        && "黄帝问答".equals(command.getTitle())
                        && "GLOBAL".equals(command.getScope())));
        assertEquals(9001L, response.getSessionId());
        assertEquals("黄帝问答", response.getTitle());
    }

    @Test
    void chatCompletionsShouldDelegateToKnowledgeQaService() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalController controller =
                new DiscoveryQaPortalController(service, knowledgeQaApplicationService);
        DiscoveryQaRequests.ChatCompletionsRequest request = new DiscoveryQaRequests.ChatCompletionsRequest();
        request.setSessionId(5001L);
        request.setModel("kuzhambu-qa");
        request.setMessages(List.of(message("user", "黄帝是谁"), message("assistant", "上古帝王")));
        request.setStream(false);
        request.setMetadata(Map.of("traceId", "trace-1"));
        request.setOptions(Map.of("temperature", 0.2));
        request.setRequestId("req-1");
        request.setTraceId("trace-1");
        when(knowledgeQaApplicationService.chatCompletion(any()))
                .thenReturn(new ChatCompletionResult(
                        5001L,
                        7001L,
                        7002L,
                        "黄帝是谁",
                        "SUCCEEDED",
                        null,
                        List.of(new ChatCompletionChoice(0, new ChatCompletionMessage("assistant", "黄帝是上古帝王"), "stop")),
                        List.of(new ChatCompletionSource(
                                "SANCAI_ENTRY:1001", "SANCAI", "SANCAI_ENTRY", "1001", "黄帝", "上古帝王", 0.88d, Map.of())),
                        new ChatUsageResult(100, 80, 180),
                        Map.of("id", "chatcmpl-1")));

        var response = controller.chatCompletions(request);

        verify(knowledgeQaApplicationService)
                .chatCompletion(argThat(command -> command != null
                        && command.getSessionId() != null
                        && command.getSessionId().equals(5001L)
                        && "kuzhambu-qa".equals(command.getModel())
                        && command.getMessages() != null
                        && command.getMessages().size() == 2));
        assertEquals("SUCCEEDED", response.getAnswerStatus());
        assertEquals("黄帝是上古帝王", response.getAnswer());
        assertEquals(1, response.getSources().size());
        assertEquals("SANCAI_ENTRY:1001", response.getSources().get(0).getSourceId());
        assertEquals("1001", response.getSources().get(0).getContentId());
        assertEquals("stop", response.getChoices().get(0).getFinishReason());
        assertEquals("chatcmpl-1", response.getRaw().get("id"));
    }

    private DiscoveryQaRequests.ChatMessage message(String role, String content) {
        DiscoveryQaRequests.ChatMessage message = new DiscoveryQaRequests.ChatMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    private void assertRequestMapping(Class<?> type, String expectedPath) {
        RequestMapping mapping = type.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private void assertPostMapping(Class<?> type, String methodName, String expectedPath, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private void assertJsonFields(Object value, String... fieldNames) throws Exception {
        var node = OBJECT_MAPPER.valueToTree(value);
        for (String fieldName : fieldNames) {
            assertTrue(node.has(fieldName), "missing field " + fieldName);
        }
    }
}
