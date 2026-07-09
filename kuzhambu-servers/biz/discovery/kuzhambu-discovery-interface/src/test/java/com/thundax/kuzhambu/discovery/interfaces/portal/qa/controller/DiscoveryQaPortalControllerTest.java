package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatCompletionChoice;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatCompletionMessage;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatCompletionSource;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatUsageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionExportResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import java.lang.reflect.Method;
import java.util.Date;
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
                "pageSessions",
                "session/page",
                DiscoveryQaRequests.QaSessionPageRequest.class);
        assertPostMapping(
                DiscoveryQaPortalController.class,
                "getSession",
                "session/get",
                DiscoveryQaRequests.QaSessionGetRequest.class);
        assertPostMapping(
                DiscoveryQaPortalController.class,
                "deleteSession",
                "session/delete",
                DiscoveryQaRequests.QaSessionDeleteRequest.class);
        assertPostMapping(
                DiscoveryQaPortalController.class,
                "exportSession",
                "session/export",
                DiscoveryQaRequests.QaSessionExportRequest.class);
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
                  "sessionId": "5001",
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
        assertEquals("5001", chatCompletionRequest.getSessionId());
        assertJsonFields(chatCompletionRequest, "sessionId", "model", "messages", "requestId", "traceId");

        DiscoveryQaRequests.QaSessionPageRequest pageRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "ownerUserId": 1001,
                  "pageNo": 1,
                  "pageSize": 20,
                  "limit": 20
                }
                """,
                DiscoveryQaRequests.QaSessionPageRequest.class);
        assertEquals(1001L, pageRequest.getOwnerUserId());
        assertJsonFields(pageRequest, "ownerUserId", "pageNo", "pageSize", "limit");

        DiscoveryQaRequests.QaSessionGetRequest getRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "sessionId": "5001",
                  "ownerUserId": 1001
                }
                """,
                DiscoveryQaRequests.QaSessionGetRequest.class);
        assertEquals("5001", getRequest.getSessionId());
        assertJsonFields(getRequest, "sessionId", "ownerUserId");

        DiscoveryQaRequests.QaSessionDeleteRequest deleteRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "sessionId": "5001",
                  "ownerUserId": 1001
                }
                """,
                DiscoveryQaRequests.QaSessionDeleteRequest.class);
        assertEquals("5001", deleteRequest.getSessionId());
        assertJsonFields(deleteRequest, "sessionId", "ownerUserId");

        DiscoveryQaRequests.QaSessionExportRequest exportRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "sessionId": "5001",
                  "ownerUserId": 1001,
                  "format": "CSV"
                }
                """,
                DiscoveryQaRequests.QaSessionExportRequest.class);
        assertEquals("5001", exportRequest.getSessionId());
        assertEquals("CSV", exportRequest.getFormat());
        assertJsonFields(exportRequest, "sessionId", "ownerUserId", "format");
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
                        null,
                        null));

        var response = controller.openSession(request);

        verify(service)
                .openSession(argThat(command -> command != null
                        && command.getOwnerUserId() != null
                        && command.getOwnerUserId().equals(1001L)
                        && "黄帝问答".equals(command.getTitle())
                        && "GLOBAL".equals(command.getScope())));
        assertEquals("9001", response.getSessionId());
        assertEquals("黄帝问答", response.getTitle());
    }

    @Test
    void openSessionShouldMapWangqiSingleDocumentContext() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalController controller =
                new DiscoveryQaPortalController(service, knowledgeQaApplicationService);
        DiscoveryQaRequests.OpenSessionRequest request = new DiscoveryQaRequests.OpenSessionRequest();
        request.setOwnerUserId(1001L);
        request.setTitle("王圻文档问答");
        request.setScope("PORTAL");
        request.setContextMode("SINGLE_DOCUMENT");
        request.setContextContentType("WANGQI_DOCUMENT");
        request.setContextContentId(3001L);
        when(service.openSession(any()))
                .thenReturn(new QaSessionResult(
                        9002L,
                        1001L,
                        "王圻文档问答",
                        "PORTAL",
                        "SINGLE_DOCUMENT",
                        "WANGQI_DOCUMENT",
                        3001L,
                        "OPEN",
                        1_718_000_000_000L,
                        null,
                        null));

        var response = controller.openSession(request);

        verify(service)
                .openSession(argThat(command -> command != null
                        && "SINGLE_DOCUMENT".equals(command.getContextMode())
                        && "WANGQI_DOCUMENT".equals(command.getContextContentType())
                        && Long.valueOf(3001L).equals(command.getContextContentId())));
        assertEquals("SINGLE_DOCUMENT", response.getContextMode());
        assertEquals("WANGQI_DOCUMENT", response.getContextContentType());
        assertEquals(3001L, response.getContextContentId());
    }

    @Test
    void pageSessionsShouldDelegateToApplicationService() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalController controller =
                new DiscoveryQaPortalController(service, knowledgeQaApplicationService);
        DiscoveryQaRequests.QaSessionPageRequest request = new DiscoveryQaRequests.QaSessionPageRequest();
        request.setOwnerUserId(1001L);
        request.setPageNo(1);
        request.setPageSize(20);
        when(service.listPortalSessions("USER", "1001", 20)).thenReturn(List.of(sessionResult()));

        var response = controller.pageSessions(request);

        verify(service).listPortalSessions("USER", "1001", 20);
        assertEquals(1, response.getPageNo());
        assertEquals(20, response.getPageSize());
        assertEquals(1L, response.getCount());
        assertEquals("5001", response.getRecords().get(0).getSessionId());
    }

    @Test
    void getSessionShouldDelegateToApplicationService() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalController controller =
                new DiscoveryQaPortalController(service, knowledgeQaApplicationService);
        DiscoveryQaRequests.QaSessionGetRequest request = new DiscoveryQaRequests.QaSessionGetRequest();
        request.setSessionId("5001");
        request.setOwnerUserId(1001L);
        when(service.getPortalSessionDetail(5001L, "USER", "1001")).thenReturn(sessionDetailResult());

        var response = controller.getSession(request);

        verify(service).getPortalSessionDetail(5001L, "USER", "1001");
        assertEquals("5001", response.getSessionId());
        assertEquals(1, response.getMessages().size());
        assertEquals("黄帝是谁", response.getMessages().get(0).getContent());
    }

    @Test
    void deleteSessionShouldDelegateToApplicationService() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalController controller =
                new DiscoveryQaPortalController(service, knowledgeQaApplicationService);
        DiscoveryQaRequests.QaSessionDeleteRequest request = new DiscoveryQaRequests.QaSessionDeleteRequest();
        request.setSessionId("5001");
        request.setOwnerUserId(1001L);

        controller.deleteSession(request);

        verify(service).deleteSession(argThat(command -> matchesDeleteCommand(command)));
    }

    @Test
    void exportSessionShouldDelegateToApplicationService() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalController controller =
                new DiscoveryQaPortalController(service, knowledgeQaApplicationService);
        DiscoveryQaRequests.QaSessionExportRequest request = new DiscoveryQaRequests.QaSessionExportRequest();
        request.setSessionId("5001");
        request.setOwnerUserId(1001L);
        request.setFormat("CSV");
        when(service.exportSession(any()))
                .thenReturn(new QaSessionExportResult(
                        7001L,
                        5001L,
                        "CSV",
                        8001L,
                        "SUCCEEDED",
                        null,
                        1_718_000_000_000L,
                        1_718_000_001_000L,
                        "discovery-qa-session-5001-7001.csv",
                        "text/csv; charset=UTF-8"));

        var response = controller.exportSession(request);

        verify(service).exportSession(argThat(command -> matchesExportCommand(command)));
        assertEquals("7001", response.getExportId());
        assertEquals("5001", response.getSessionId());
        assertEquals("CSV", response.getFormat());
        assertEquals("8001", response.getStorageObjectId());
        assertEquals("SUCCEEDED", response.getExportStatus());
        assertEquals(1_718_000_000_000L, response.getRequestedAt());
        assertEquals(1_718_000_001_000L, response.getCompletedAt());
        assertEquals("discovery-qa-session-5001-7001.csv", response.getFilename());
        assertEquals("text/csv; charset=UTF-8", response.getContentType());
    }

    @Test
    void chatCompletionsShouldDelegateToKnowledgeQaService() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalController controller =
                new DiscoveryQaPortalController(service, knowledgeQaApplicationService);
        DiscoveryQaRequests.ChatCompletionsRequest request = new DiscoveryQaRequests.ChatCompletionsRequest();
        request.setSessionId("5001");
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

    private static boolean matchesDeleteCommand(DeleteQaSessionCommand command) {
        return command != null
                && Long.valueOf(5001L).equals(command.getSessionId())
                && "USER".equals(command.getOwnerType())
                && "1001".equals(command.getOwnerId())
                && Boolean.FALSE.equals(command.getAdminOperation());
    }

    private static boolean matchesExportCommand(ExportQaSessionCommand command) {
        return command != null
                && Long.valueOf(5001L).equals(command.getSessionId())
                && Long.valueOf(1001L).equals(command.getRequesterUserId())
                && "USER".equals(command.getOwnerType())
                && "1001".equals(command.getOwnerId())
                && Boolean.FALSE.equals(command.getAdminOperation())
                && "CSV".equals(command.getFormat());
    }

    private static QaSessionResult sessionResult() {
        return new QaSessionResult(
                5001L,
                1001L,
                "黄帝问答",
                "GLOBAL",
                "SEARCH",
                "SANCAI_ENTRY",
                10001L,
                "OPEN",
                1_718_000_000_000L,
                1_718_000_001_000L,
                null);
    }

    private static QaSessionDetailResult sessionDetailResult() {
        QaSessionDetailResult result = new QaSessionDetailResult();
        result.setSessionId(5001L);
        result.setOwnerUserId(1001L);
        result.setTitle("黄帝问答");
        result.setScope("GLOBAL");
        result.setContextMode("SEARCH");
        result.setContextContentType("SANCAI_ENTRY");
        result.setContextContentId(10001L);
        result.setStatus("OPEN");
        result.setOpenedAt(1_718_000_000_000L);
        result.setLastMessageAt(1_718_000_001_000L);
        result.setMessages(List.of(new QaMessageResult(
                7001L, 5001L, "user", "黄帝是谁", "SENT", 0, null, new Date(1_718_000_001_000L), null)));
        return result;
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
