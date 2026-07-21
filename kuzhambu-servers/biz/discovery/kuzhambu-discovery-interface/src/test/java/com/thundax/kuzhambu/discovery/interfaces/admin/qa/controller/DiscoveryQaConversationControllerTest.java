package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatCompletionChoice;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatCompletionMessage;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult.ChatUsageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoveryQaConversationControllerTest {

    @Test
    void routesShouldKeepAdminConversationApiPaths() throws Exception {
        assertRequestMapping(DiscoveryQaConversationController.class, "/api/discovery/qa");
        assertPostMapping(
                DiscoveryQaConversationController.class,
                "openSession",
                "session/open",
                DiscoveryQaRequests.OpenSessionRequest.class);
        assertPostMapping(
                DiscoveryQaConversationController.class,
                "pageSessions",
                "session/page",
                DiscoveryQaRequests.QaSessionPageRequest.class);
        assertPostMapping(
                DiscoveryQaConversationController.class,
                "getSession",
                "session/get",
                DiscoveryQaRequests.QaSessionGetRequest.class);
        assertPostMapping(
                DiscoveryQaConversationController.class,
                "deleteSession",
                "session/delete",
                DiscoveryQaRequests.QaSessionDeleteRequest.class);
        assertPostMapping(
                DiscoveryQaConversationController.class,
                "exportSession",
                "session/export",
                DiscoveryQaRequests.QaSessionExportRequest.class);
        assertPostMapping(
                DiscoveryQaConversationController.class,
                "chatCompletions",
                "chat/completions",
                DiscoveryQaRequests.ChatCompletionsRequest.class);
        assertPostMappingProduces(
                DiscoveryQaConversationStreamController.class,
                "chatCompletionsStream",
                "chat/completions/stream",
                MediaType.TEXT_EVENT_STREAM_VALUE,
                DiscoveryQaRequests.ChatCompletionsRequest.class);
        assertHasPermission(
                DiscoveryQaConversationController.class,
                "openSession",
                "discovery:qa:view",
                DiscoveryQaRequests.OpenSessionRequest.class);
        assertHasPermission(
                DiscoveryQaConversationController.class,
                "deleteSession",
                "discovery:qa:edit",
                DiscoveryQaRequests.QaSessionDeleteRequest.class);
    }

    @Test
    void openSessionShouldDelegateToApplicationService() {
        QaApplicationService qaApplicationService = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaConversationController controller =
                new DiscoveryQaConversationController(qaApplicationService, knowledgeQaApplicationService);
        DiscoveryQaRequests.OpenSessionRequest request = new DiscoveryQaRequests.OpenSessionRequest();
        request.setOwnerUserId(1001L);
        request.setTitle("知识助手");
        request.setScope("GLOBAL");

        when(qaApplicationService.openSession(any()))
                .thenReturn(new QaSessionResult(
                        9001L, 1001L, "知识助手", "GLOBAL", null, null, null, "OPEN", 1_718_000_000_000L, null, null));

        var response = controller.openSession(request);

        verify(qaApplicationService)
                .openSession(argThat(command -> command != null
                        && Long.valueOf(1001L).equals(command.getOwnerUserId())
                        && "知识助手".equals(command.getTitle())
                        && "GLOBAL".equals(command.getScope())));
        assertEquals("9001", response.getSessionId());
        assertEquals("知识助手", response.getTitle());
    }

    @Test
    void chatCompletionsStreamShouldDelegateAsStreamingProviderCall() {
        QaApplicationService qaApplicationService = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaConversationStreamController controller =
                new DiscoveryQaConversationStreamController(knowledgeQaApplicationService, Runnable::run);
        DiscoveryQaRequests.ChatCompletionsRequest request = new DiscoveryQaRequests.ChatCompletionsRequest();
        request.setSessionId("5001");
        request.setModel("kuzhambu-qa");
        request.setMessages(List.of(message("user", "礼学是什么？")));
        request.setStream(true);

        when(knowledgeQaApplicationService.chatCompletionStream(any(), any()))
                .thenReturn(new ChatCompletionResult(
                        5001L,
                        7001L,
                        7002L,
                        "礼学是什么？",
                        "SUCCEEDED",
                        null,
                        List.of(new ChatCompletionChoice(0, new ChatCompletionMessage("assistant", "礼学是礼制之学"), "stop")),
                        List.of(),
                        new ChatUsageResult(100, 80, 180),
                        Map.of("id", "chatcmpl-1")));

        var emitter = controller.chatCompletionsStream(request);

        assertTrue(emitter != null);
        verify(knowledgeQaApplicationService, timeout(1000))
                .chatCompletionStream(
                        argThat(command -> command != null
                                && Long.valueOf(5001L).equals(command.getSessionId())
                                && command.isStream()),
                        any());
    }

    @Test
    void chatCompletionsStreamShouldSanitizeProviderErrors() throws Exception {
        QaApplicationService qaApplicationService = mock(QaApplicationService.class);
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        DiscoveryQaConversationStreamController controller =
                new DiscoveryQaConversationStreamController(knowledgeQaApplicationService, Runnable::run);
        DiscoveryQaRequests.ChatCompletionsRequest request = new DiscoveryQaRequests.ChatCompletionsRequest();
        request.setSessionId("5001");
        request.setModel("kuzhambu-qa");
        request.setMessages(List.of(message("user", "礼学是什么？")));
        when(knowledgeQaApplicationService.chatCompletionStream(any(), any()))
                .thenThrow(new IllegalStateException(
                        "500 Internal Server Error on POST request: {\"message\":\"appId is empty\"}"));

        controller.chatCompletionsStream(request);

        verify(knowledgeQaApplicationService, timeout(1000)).chatCompletionStream(any(), any());
        Method method = DiscoveryQaConversationStreamController.class.getDeclaredMethod(
                "toClientErrorMessage", Exception.class);
        method.setAccessible(true);
        assertEquals(
                "问答应用未配置，请检查 FastGPT 应用配置。", method.invoke(controller, new IllegalStateException("appId is empty")));
    }

    @Test
    void chatCompletionsStreamShouldSubmitWorkToInjectedExecutor() {
        KnowledgeQaApplicationService knowledgeQaApplicationService = mock(KnowledgeQaApplicationService.class);
        RecordingExecutor executor = new RecordingExecutor();
        DiscoveryQaConversationStreamController controller =
                new DiscoveryQaConversationStreamController(knowledgeQaApplicationService, executor);
        DiscoveryQaRequests.ChatCompletionsRequest request = new DiscoveryQaRequests.ChatCompletionsRequest();
        request.setSessionId("5001");

        controller.chatCompletionsStream(request);

        assertTrue(executor.submitted);
        verify(knowledgeQaApplicationService, org.mockito.Mockito.never()).chatCompletionStream(any(), any());
    }

    private static DiscoveryQaRequests.ChatMessage message(String role, String content) {
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

    private static void assertRequestMapping(Class<?> type, String expectedPath) {
        RequestMapping mapping = type.getAnnotation(RequestMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMapping(Class<?> type, String methodName, String expectedPath, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
    }

    private static void assertPostMappingProduces(
            Class<?> type, String methodName, String expectedPath, String expectedProduces, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(expectedPath, mapping.value()[0]);
        assertArrayEquals(new String[] {expectedProduces}, mapping.produces());
    }

    private static void assertHasPermission(Class<?> type, String methodName, String permission, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        HasPermission annotation = method.getAnnotation(HasPermission.class);
        assertTrue(annotation != null);
        assertArrayEquals(new String[] {permission}, annotation.value());
    }
}
