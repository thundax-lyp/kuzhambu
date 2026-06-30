package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request.DiscoveryQaAdminRequests;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoveryQaAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminQaApiPaths() throws Exception {
        assertRequestMapping(DiscoveryQaAdminController.class, "/api/discovery/qa-admin");
        assertPostMapping(
                DiscoveryQaAdminController.class,
                "getSession",
                "session/get",
                DiscoveryQaAdminRequests.QaSessionGetRequest.class);
        assertPostMapping(
                DiscoveryQaAdminController.class,
                "listSources",
                "source/list",
                DiscoveryQaAdminRequests.QaSourceListRequest.class);
        assertPostMapping(
                DiscoveryQaAdminController.class,
                "getTrace",
                "trace/get",
                DiscoveryQaAdminRequests.QaTraceGetRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        DiscoveryQaAdminRequests.QaSessionGetRequest sessionGetRequest = OBJECT_MAPPER.readValue(
                """
                {"sessionId":5001}
                """,
                DiscoveryQaAdminRequests.QaSessionGetRequest.class);
        assertEquals(5001L, sessionGetRequest.getSessionId());
        assertJsonFields(sessionGetRequest, "sessionId");

        DiscoveryQaAdminRequests.QaSourceListRequest sourceListRequest = OBJECT_MAPPER.readValue(
                """
                {"messageId":7002}
                """,
                DiscoveryQaAdminRequests.QaSourceListRequest.class);
        assertEquals(7002L, sourceListRequest.getMessageId());
        assertJsonFields(sourceListRequest, "messageId");

        DiscoveryQaAdminRequests.QaTraceGetRequest traceGetRequest = OBJECT_MAPPER.readValue(
                """
                {"traceId":8001}
                """,
                DiscoveryQaAdminRequests.QaTraceGetRequest.class);
        assertEquals(8001L, traceGetRequest.getTraceId());
        assertJsonFields(traceGetRequest, "traceId");
    }

    @Test
    void endpointsShouldDelegateToApplicationService() {
        QaApplicationService service = mock(QaApplicationService.class);
        DiscoveryQaAdminController controller = new DiscoveryQaAdminController(service);
        when(service.getSessionDetail(5001L)).thenReturn(sampleSessionDetail());
        when(service.listSourcesByMessageId(7002L)).thenReturn(List.of(sampleSource()));
        when(service.getTraceByTraceId(8001L)).thenReturn(sampleTrace());

        DiscoveryQaAdminRequests.QaSessionGetRequest sessionRequest =
                new DiscoveryQaAdminRequests.QaSessionGetRequest();
        sessionRequest.setSessionId(5001L);
        var sessionResponse = controller.getSession(sessionRequest);
        assertEquals(5001L, sessionResponse.getSessionId());

        DiscoveryQaAdminRequests.QaSourceListRequest sourceRequest = new DiscoveryQaAdminRequests.QaSourceListRequest();
        sourceRequest.setMessageId(7002L);
        var sources = controller.listSources(sourceRequest);
        assertEquals(1, sources.size());
        assertEquals(9001L, sources.get(0).getSourceId());

        DiscoveryQaAdminRequests.QaTraceGetRequest traceRequest = new DiscoveryQaAdminRequests.QaTraceGetRequest();
        traceRequest.setTraceId(8001L);
        var traceResponse = controller.getTrace(traceRequest);
        assertEquals(8001L, traceResponse.getTraceId());

        verify(service).getSessionDetail(argThat(sessionId -> sessionId != null && sessionId.equals(5001L)));
        verify(service).listSourcesByMessageId(argThat(messageId -> messageId != null && messageId.equals(7002L)));
        verify(service).getTraceByTraceId(argThat(traceId -> traceId != null && traceId.equals(8001L)));
    }

    private QaSessionDetailResult sampleSessionDetail() {
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
        result.setLastMessageAt(1_718_000_100_000L);
        result.setMessages(List.of(new QaMessageResult(
                7001L, 5001L, "USER", "黄帝是谁", "SENT", 1, null, new Date(1_718_000_050_000L), null)));
        return result;
    }

    private QaSourceResult sampleSource() {
        return new QaSourceResult(
                9001L, "SANCAI_ENTRY", 1001L, "SANCAI", "黄帝", "卷一", "上古帝王", 1, BigDecimal.ONE, "CITED");
    }

    private QaTraceResult sampleTrace() {
        return new QaTraceResult(
                8001L,
                7002L,
                6001L,
                "黄帝是谁",
                "黄帝是谁",
                "GLOBAL",
                "{\"sessionId\":5001}",
                "[\"轩辕\"]",
                "[{\"name\":\"黄帝\"}]",
                1,
                "{\"sources\":[]}",
                new Date(1_718_000_070_000L));
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
