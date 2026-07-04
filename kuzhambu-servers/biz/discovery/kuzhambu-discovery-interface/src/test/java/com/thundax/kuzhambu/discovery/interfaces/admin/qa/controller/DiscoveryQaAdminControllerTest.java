package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.KnowledgeSyncItemPageQuery;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeHealthResult;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeSyncItemResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeSyncApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request.DiscoveryQaAdminRequests;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
                "deleteSession",
                "session/delete",
                DiscoveryQaAdminRequests.QaSessionDeleteRequest.class);
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
        assertPostMapping(DiscoveryQaAdminController.class, "getKnowledgeHealth", "knowledge/health");
        assertPostMapping(DiscoveryQaAdminController.class, "rebuildKnowledge", "knowledge/rebuild");
        assertPostMapping(
                DiscoveryQaAdminController.class,
                "syncKnowledge",
                "knowledge/sync",
                DiscoveryQaAdminRequests.KnowledgeSyncRequest.class);
        assertPostMapping(
                DiscoveryQaAdminController.class,
                "pageKnowledgeSyncItems",
                "knowledge/sync/page",
                DiscoveryQaAdminRequests.KnowledgeSyncPageRequest.class);
        assertHasPermission(
                DiscoveryQaAdminController.class,
                "deleteSession",
                "discovery:qa:edit",
                DiscoveryQaAdminRequests.QaSessionDeleteRequest.class);
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

        DiscoveryQaAdminRequests.QaSessionDeleteRequest sessionDeleteRequest = OBJECT_MAPPER.readValue(
                """
                        {"sessionId":5001}
                        """,
                DiscoveryQaAdminRequests.QaSessionDeleteRequest.class);
        assertEquals(5001L, sessionDeleteRequest.getSessionId());
        assertJsonFields(sessionDeleteRequest, "sessionId");

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

        DiscoveryQaAdminRequests.KnowledgeSyncRequest syncRequest = OBJECT_MAPPER.readValue(
                """
                        {"contentType":"SANCAI_ENTRY","contentId":10001,"currentVersionNo":3}
                        """,
                DiscoveryQaAdminRequests.KnowledgeSyncRequest.class);
        assertEquals("SANCAI_ENTRY", syncRequest.getContentType());
        assertEquals(10001L, syncRequest.getContentId());
        assertEquals(3, syncRequest.getCurrentVersionNo());
        assertJsonFields(syncRequest, "contentType", "contentId");

        DiscoveryQaAdminRequests.KnowledgeSyncPageRequest syncPageRequest = OBJECT_MAPPER.readValue(
                """
                        {"contentType":"SANCAI_ENTRY","syncStatus":"FAILED","pageNo":2,"pageSize":10}
                        """,
                DiscoveryQaAdminRequests.KnowledgeSyncPageRequest.class);
        assertEquals("SANCAI_ENTRY", syncPageRequest.getContentType());
        assertEquals("FAILED", syncPageRequest.getSyncStatus());
        assertEquals(2, syncPageRequest.getPageNo());
        assertEquals(10, syncPageRequest.getPageSize());
        assertJsonFields(syncPageRequest, "contentType", "syncStatus", "pageNo", "pageSize");
    }

    @Test
    void endpointsShouldDelegateToApplicationService() {
        QaApplicationService qaService = mock(QaApplicationService.class);
        KnowledgeSyncApplicationService syncService = mock(KnowledgeSyncApplicationService.class);
        DiscoveryQaAdminController controller = new DiscoveryQaAdminController(qaService, syncService);

        when(qaService.getSessionDetail(5001L)).thenReturn(sampleSessionDetail());
        when(qaService.listSourcesByMessageId(7002L)).thenReturn(List.of(sampleSource()));
        when(qaService.getTraceByTraceId(8001L)).thenReturn(sampleTrace());
        when(syncService.health())
                .thenReturn(new KnowledgeHealthResult(true, "fastgpt", null, Map.of("provider", "fastgpt")));
        when(syncService.rebuild()).thenReturn(9001L);
        when(syncService.syncContent(any(SyncKnowledgeContentCommand.class))).thenReturn(sampleSyncItem());
        when(syncService.pageSyncItems(any(KnowledgeSyncItemPageQuery.class)))
                .thenReturn(PageResult.of(2, 10, 1, List.of(sampleSyncItem())));

        DiscoveryQaAdminRequests.QaSessionGetRequest sessionRequest =
                new DiscoveryQaAdminRequests.QaSessionGetRequest();
        sessionRequest.setSessionId(5001L);
        var sessionResponse = controller.getSession(sessionRequest);
        assertEquals(5001L, sessionResponse.getSessionId());
        assertEquals(1_718_000_200_000L, sessionResponse.getRemovedAt());

        DiscoveryQaAdminRequests.QaSessionDeleteRequest deleteRequest =
                new DiscoveryQaAdminRequests.QaSessionDeleteRequest();
        deleteRequest.setSessionId(5001L);
        controller.deleteSession(deleteRequest);

        DiscoveryQaAdminRequests.QaSourceListRequest sourceRequest = new DiscoveryQaAdminRequests.QaSourceListRequest();
        sourceRequest.setMessageId(7002L);
        var sources = controller.listSources(sourceRequest);
        assertEquals(1, sources.size());
        assertEquals(9001L, sources.get(0).getSourceId());

        DiscoveryQaAdminRequests.QaTraceGetRequest traceRequest = new DiscoveryQaAdminRequests.QaTraceGetRequest();
        traceRequest.setTraceId(8001L);
        var traceResponse = controller.getTrace(traceRequest);
        assertNotNull(traceResponse);
        assertEquals(8001L, traceResponse.getTraceId());

        var healthResponse = controller.getKnowledgeHealth();
        assertEquals("AVAILABLE", healthResponse.getStatus());
        assertEquals("fastgpt", healthResponse.getProvider());

        var rebuildBatchId = controller.rebuildKnowledge();
        assertEquals(9001L, rebuildBatchId);

        DiscoveryQaAdminRequests.KnowledgeSyncRequest syncRequest = new DiscoveryQaAdminRequests.KnowledgeSyncRequest();
        syncRequest.setContentType("SANCAI_ENTRY");
        syncRequest.setContentId(10001L);
        syncRequest.setCurrentVersionNo(3);
        var syncResponse = controller.syncKnowledge(syncRequest);
        assertNotNull(syncResponse);
        assertEquals("SANCAI_ENTRY", syncResponse.getContentType());
        assertEquals("SYNCHRONIZED", syncResponse.getSyncStatus());

        DiscoveryQaAdminRequests.KnowledgeSyncPageRequest syncPageRequest =
                new DiscoveryQaAdminRequests.KnowledgeSyncPageRequest();
        syncPageRequest.setContentType("SANCAI_ENTRY");
        syncPageRequest.setSyncStatus("FAILED");
        syncPageRequest.setPageNo(2);
        syncPageRequest.setPageSize(10);
        var syncPageResponse = controller.pageKnowledgeSyncItems(syncPageRequest);
        assertNotNull(syncPageResponse);
        assertEquals(2, syncPageResponse.getPageNo());
        assertEquals(10, syncPageResponse.getPageSize());
        assertEquals(1, syncPageResponse.getCount());

        verify(qaService).getSessionDetail(5001L);
        ArgumentCaptor<DeleteQaSessionCommand> deleteCommand = ArgumentCaptor.forClass(DeleteQaSessionCommand.class);
        verify(qaService).deleteSession(deleteCommand.capture());
        assertEquals(5001L, deleteCommand.getValue().getSessionId());
        assertEquals(Boolean.TRUE, deleteCommand.getValue().getAdminOperation());
        verify(qaService).listSourcesByMessageId(7002L);
        verify(qaService).getTraceByTraceId(8001L);

        verify(syncService).health();
        verify(syncService).rebuild();

        ArgumentCaptor<SyncKnowledgeContentCommand> syncContentCommand =
                ArgumentCaptor.forClass(SyncKnowledgeContentCommand.class);
        verify(syncService).syncContent(syncContentCommand.capture());
        assertEquals("SANCAI_ENTRY", syncContentCommand.getValue().getContentType());
        assertEquals(10001L, syncContentCommand.getValue().getContentId());
        assertEquals(3, syncContentCommand.getValue().getCurrentVersionNo());

        ArgumentCaptor<KnowledgeSyncItemPageQuery> syncPageQuery =
                ArgumentCaptor.forClass(KnowledgeSyncItemPageQuery.class);
        verify(syncService).pageSyncItems(syncPageQuery.capture());
        assertEquals("SANCAI_ENTRY", syncPageQuery.getValue().getContentType());
        assertEquals("FAILED", syncPageQuery.getValue().getSyncStatus());
        assertEquals(2, syncPageQuery.getValue().getPageNo());
        assertEquals(10, syncPageQuery.getValue().getPageSize());
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
        result.setRemovedAt(1_718_000_200_000L);
        result.setMessages(List.of(new QaMessageResult(
                7001L, 5001L, "USER", "黄帝是谁", "SENT", 1, null, new Date(1_718_000_050_000L), null)));
        return result;
    }

    private QaSourceResult sampleSource() {
        return new QaSourceResult(
                9001L, "SANCAI_ENTRY", 1001L, "SANCAI", "黄帝", "卷一", "上古帝王", 1, BigDecimal.ONE, "CITED");
    }

    private QaTraceResult sampleTrace() {
        QaTraceResult trace = new QaTraceResult();
        trace.setTraceId(8001L);
        trace.setMessageId(7002L);
        trace.setRawQuestion("黄帝是谁");
        trace.setProvider("kuzhambu-qa");
        trace.setExternalKnowledgeBaseId("kb-1");
        trace.setExternalKnowledgeItemIds("[\"item-1\",\"item-2\"]");
        trace.setExternalChatId("chat-1");
        trace.setProviderRequestId("1001");
        trace.setLatencyMs(120L);
        trace.setFailureReason("none");
        trace.setRaw("{\"foo\":\"bar\"}");
        trace.setRetrievedAt(new Date(1_718_000_070_000L));
        return trace;
    }

    private KnowledgeSyncItemResult sampleSyncItem() {
        return new KnowledgeSyncItemResult(
                "SANCAI_ENTRY:10001",
                "SANCAI_ENTRY",
                10001L,
                "kuzhambu-qa",
                3,
                "hash-xxx",
                "fastgpt",
                "kb-1",
                "item-1",
                "SYNCHRONIZED",
                null,
                1_718_000_100_000L,
                1_718_000_000_000L,
                1_718_000_050_000L);
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

    private void assertHasPermission(Class<?> type, String methodName, String permission, Class<?>... parameters)
            throws Exception {
        Method method = type.getDeclaredMethod(methodName, parameters);
        HasPermission annotation = method.getAnnotation(HasPermission.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[] {permission}, annotation.value());
    }

    private void assertJsonFields(Object value, String... fieldNames) throws Exception {
        var node = OBJECT_MAPPER.valueToTree(value);
        for (String fieldName : fieldNames) {
            if (!node.has(fieldName)) {
                fail("missing field " + fieldName);
            }
        }
    }
}
