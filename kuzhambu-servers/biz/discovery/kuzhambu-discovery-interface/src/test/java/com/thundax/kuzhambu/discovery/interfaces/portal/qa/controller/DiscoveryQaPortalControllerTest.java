package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoveryQaPortalControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepPortalQaApiPaths() throws Exception {
        assertRequestMapping(DiscoveryQaPortalController.class, "/api/portal/discovery/qa");
        assertPostMapping(
                DiscoveryQaPortalController.class,
                "initSession",
                "session/init",
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
                "downloadSession",
                "session/download",
                DiscoveryQaRequests.QaSessionExportRequest.class);
        assertPostMapping(
                DiscoveryQaPortalController.class,
                "createChatCompletion",
                "chat/create",
                DiscoveryQaRequests.ChatCompletionsRequest.class);
        assertPostMappingProduces(
                DiscoveryQaPortalStreamController.class,
                "submitChatCompletion",
                "chat/submit",
                MediaType.TEXT_EVENT_STREAM_VALUE,
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
    void sessionRequestsShouldRejectNonnumericSessionId() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        DiscoveryQaRequests.QaSessionGetRequest getRequest = new DiscoveryQaRequests.QaSessionGetRequest();
        getRequest.setSessionId("stored-5001");
        getRequest.setOwnerUserId(1001L);
        DiscoveryQaRequests.QaSessionDeleteRequest deleteRequest = new DiscoveryQaRequests.QaSessionDeleteRequest();
        deleteRequest.setSessionId("stored-5001");
        deleteRequest.setOwnerUserId(1001L);
        DiscoveryQaRequests.QaSessionExportRequest exportRequest = new DiscoveryQaRequests.QaSessionExportRequest();
        exportRequest.setSessionId("stored-5001");
        exportRequest.setOwnerUserId(1001L);
        DiscoveryQaRequests.ChatCompletionsRequest chatRequest = new DiscoveryQaRequests.ChatCompletionsRequest();
        chatRequest.setSessionId("stored-5001");

        assertFalse(validator.validate(getRequest).isEmpty());
        assertFalse(validator.validate(deleteRequest).isEmpty());
        assertFalse(validator.validate(exportRequest).isEmpty());
        assertFalse(validator.validate(chatRequest).isEmpty());
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

        var response = controller.initSession(request);

        verify(service)
                .openSession(argThat(command -> command != null
                        && command.ownerUserId() != null
                        && command.ownerUserId().equals(1001L)
                        && "黄帝问答".equals(command.title())
                        && "GLOBAL".equals(command.scope())));
        assertEquals("9001", response.getId());
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

        var response = controller.initSession(request);

        verify(service)
                .openSession(argThat(command -> command != null
                        && "SINGLE_DOCUMENT".equals(command.contextMode())
                        && "WANGQI_DOCUMENT".equals(command.contextContentType())
                        && Long.valueOf(3001L).equals(command.contextContentId())));
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
        when(service.listPortalSessions(any(), any())).thenReturn(List.of(sessionResult()));

        var response = controller.pageSessions(request);

        verify(service)
                .listPortalSessions(
                        argThat(query ->
                                query != null && "USER".equals(query.ownerType()) && "1001".equals(query.ownerId())),
                        argThat(pageQuery ->
                                pageQuery != null && pageQuery.getPageNo() == 1 && pageQuery.getPageSize() == 20));
        assertEquals(1, response.getPageNo());
        assertEquals(20, response.getPageSize());
        assertEquals(1L, response.getCount());
        assertEquals("5001", response.getRecords().get(0).getId());
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
        when(service.getPortalSessionDetail(any())).thenReturn(sessionDetailResult());

        var response = controller.getSession(request);

        verify(service)
                .getPortalSessionDetail(argThat(query -> query != null
                        && query.sessionId() == 5001L
                        && "USER".equals(query.ownerType())
                        && "1001".equals(query.ownerId())));
        assertEquals("5001", response.getId());
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

        var response = controller.downloadSession(request);

        verify(service).exportSession(argThat(command -> matchesExportCommand(command)));
        assertEquals("7001", response.getId());
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
    void createChatCompletionShouldDelegateToKnowledgeQaService() {
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

        var response = controller.createChatCompletion(request);

        verify(knowledgeQaApplicationService)
                .chatCompletion(argThat(command -> command != null
                        && command.sessionId() != null
                        && command.sessionId().equals(5001L)
                        && "kuzhambu-qa".equals(command.model())
                        && command.messages() != null
                        && command.messages().size() == 2));
        assertEquals("SUCCEEDED", response.getAnswerStatus());
        assertEquals("黄帝是上古帝王", response.getAnswer());
        assertEquals(1, response.getSources().size());
        assertEquals("SANCAI_ENTRY:1001", response.getSources().get(0).getSourceId());
        assertEquals("1001", response.getSources().get(0).getContentId());
        assertEquals("stop", response.getChoices().get(0).getFinishReason());
        assertEquals("chatcmpl-1", response.getRaw().get("id"));
    }

    @Test
    void submitChatCompletionShouldDelegateAsStreamingProviderCall() {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalStreamController controller =
                new DiscoveryQaPortalStreamController(knowledgeQaApplicationService, Runnable::run);
        DiscoveryQaRequests.ChatCompletionsRequest request = new DiscoveryQaRequests.ChatCompletionsRequest();
        request.setSessionId("5001");
        request.setModel("kuzhambu-qa");
        request.setMessages(List.of(message("user", "黄帝是谁")));
        request.setStream(true);
        when(knowledgeQaApplicationService.chatCompletionStream(any(), any()))
                .thenReturn(new ChatCompletionResult(
                        5001L,
                        7001L,
                        7002L,
                        "黄帝是谁",
                        "SUCCEEDED",
                        null,
                        List.of(new ChatCompletionChoice(0, new ChatCompletionMessage("assistant", "黄帝是上古帝王"), "stop")),
                        List.of(),
                        new ChatUsageResult(100, 80, 180),
                        Map.of("id", "chatcmpl-1")));

        var emitter = controller.submitChatCompletion(request);

        assertTrue(emitter != null);
        verify(knowledgeQaApplicationService, timeout(1000))
                .chatCompletionStream(
                        argThat(command ->
                                command != null && Long.valueOf(5001L).equals(command.sessionId()) && command.stream()),
                        any());
    }

    @Test
    void submitChatCompletionShouldSanitizeProviderErrors() throws Exception {
        QaApplicationService service = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaPortalStreamController controller =
                new DiscoveryQaPortalStreamController(knowledgeQaApplicationService, Runnable::run);
        DiscoveryQaRequests.ChatCompletionsRequest request = new DiscoveryQaRequests.ChatCompletionsRequest();
        request.setSessionId("5001");
        request.setModel("kuzhambu-qa");
        request.setMessages(List.of(message("user", "黄帝是谁")));
        when(knowledgeQaApplicationService.chatCompletionStream(any(), any()))
                .thenThrow(new IllegalStateException("500 Internal Server Error: appId is empty"));

        var emitter = controller.submitChatCompletion(request);

        assertTrue(emitter != null);
        verify(knowledgeQaApplicationService, timeout(1000)).chatCompletionStream(any(), any());
        Method method = DiscoveryQaPortalStreamController.class.getDeclaredMethod("toClientErrorMessage");
        method.setAccessible(true);
        assertEquals("问答生成失败，请稍后重试。", method.invoke(controller));
    }

    @Test
    void submitChatCompletionShouldSubmitWorkToInjectedExecutor() {
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        RecordingExecutor executor = new RecordingExecutor();
        DiscoveryQaPortalStreamController controller =
                new DiscoveryQaPortalStreamController(knowledgeQaApplicationService, executor);
        DiscoveryQaRequests.ChatCompletionsRequest request = new DiscoveryQaRequests.ChatCompletionsRequest();
        request.setSessionId("5001");

        controller.submitChatCompletion(request);

        assertTrue(executor.submitted);
        verify(knowledgeQaApplicationService, org.mockito.Mockito.never()).chatCompletionStream(any(), any());
    }

    private DiscoveryQaRequests.ChatMessage message(String role, String content) {
        DiscoveryQaRequests.ChatMessage message = new DiscoveryQaRequests.ChatMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    private static final class RecordingExecutor implements Executor {

        private boolean submitted;

        @Override
        public void execute(Runnable command) {
            submitted = true;
        }
    }

    private static boolean matchesDeleteCommand(DeleteQaSessionCommand command) {
        return command != null
                && Long.valueOf(5001L).equals(command.sessionId())
                && "USER".equals(command.ownerType())
                && "1001".equals(command.ownerId())
                && Boolean.FALSE.equals(command.adminOperation());
    }

    private static boolean matchesExportCommand(ExportQaSessionCommand command) {
        return command != null
                && Long.valueOf(5001L).equals(command.sessionId())
                && Long.valueOf(1001L).equals(command.requesterUserId())
                && "USER".equals(command.ownerType())
                && "1001".equals(command.ownerId())
                && Boolean.FALSE.equals(command.adminOperation())
                && "CSV".equals(command.format());
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
                7001L, 5001L, "user", "黄帝是谁", "SENT", 0, null, Instant.ofEpochMilli(1_718_000_001_000L), null)));
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

    private void assertPostMappingProduces(
            Class<?> type, String methodName, String expectedPath, String expectedProduces, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        assertEquals(expectedProduces, mapping.produces()[0]);
    }

    private void assertJsonFields(Object value, String... fieldNames) throws Exception {
        var node = OBJECT_MAPPER.valueToTree(value);
        for (String fieldName : fieldNames) {
            assertTrue(node.has(fieldName), "missing field " + fieldName);
        }
    }
}
