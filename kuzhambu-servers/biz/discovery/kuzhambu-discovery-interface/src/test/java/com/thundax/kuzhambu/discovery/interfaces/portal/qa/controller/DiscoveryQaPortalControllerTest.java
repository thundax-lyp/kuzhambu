package com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.discovery.application.qa.result.QaAnswerResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.portal.qa.controller.request.DiscoveryQaRequests;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
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
                "askQuestion",
                "question/ask",
                DiscoveryQaRequests.AskQuestionRequest.class);
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

        DiscoveryQaRequests.AskQuestionRequest askQuestionRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "sessionId": 5001,
                  "question": "黄帝是谁",
                  "contextTurnCount": 1,
                  "operatorType": "USER",
                  "operatorId": "1001",
                  "requestId": "req-1",
                  "traceId": "trace-1"
                }
                """,
                DiscoveryQaRequests.AskQuestionRequest.class);
        assertEquals(5001L, askQuestionRequest.getSessionId());
        assertJsonFields(
                askQuestionRequest,
                "sessionId",
                "question",
                "contextTurnCount",
                "operatorType",
                "operatorId",
                "requestId",
                "traceId");
    }

    @Test
    void openSessionShouldDelegateToApplicationService() {
        QaApplicationService service = mock(QaApplicationService.class);
        DiscoveryQaPortalController controller = new DiscoveryQaPortalController(service);
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
    void askQuestionShouldDelegateToApplicationService() {
        QaApplicationService service = mock(QaApplicationService.class);
        DiscoveryQaPortalController controller = new DiscoveryQaPortalController(service);
        DiscoveryQaRequests.AskQuestionRequest request = new DiscoveryQaRequests.AskQuestionRequest();
        request.setSessionId(5001L);
        request.setQuestion("黄帝是谁");
        request.setContextTurnCount(1);
        request.setOperatorType("USER");
        request.setOperatorId("1001");
        request.setRequestId("req-1");
        request.setTraceId("trace-1");
        when(service.askQuestion(any()))
                .thenReturn(new QaAnswerResult(
                        5001L,
                        7001L,
                        7002L,
                        "黄帝是谁",
                        "黄帝是上古帝王",
                        "SUCCEEDED",
                        null,
                        List.of(new QaSourceResult(
                                9001L,
                                "SANCAI_ENTRY",
                                1001L,
                                "SANCAI",
                                "黄帝",
                                "卷一",
                                "上古帝王",
                                1,
                                BigDecimal.ONE,
                                "CITED")),
                        new QaAnswerResult.TraceSummaryResult(8001L, "黄帝是谁", 1, "[\"轩辕\"]", "[{\"name\":\"黄帝\"}]")));

        var response = controller.askQuestion(request);

        verify(service)
                .askQuestion(argThat(command -> command != null
                        && command.getSessionId() != null
                        && command.getSessionId().equals(5001L)
                        && "黄帝是谁".equals(command.getQuestion())
                        && "USER".equals(command.getOperatorType())));
        assertEquals("SUCCEEDED", response.getAnswerStatus());
        assertEquals(1, response.getSources().size());
        assertEquals(9001L, response.getSources().get(0).getSourceId());
        assertEquals(8001L, response.getTraceSummary().getTraceId());
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
