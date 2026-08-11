package com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.KnowledgeSyncItemQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionDetailQuery;
import com.thundax.kuzhambu.discovery.application.qa.query.QaSessionQuery;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeHealthResult;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeSyncItemResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionExportResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeSyncApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.qa.controller.request.DiscoveryQaAdminRequests;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoveryQaAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

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
                "pageSessions",
                "session/page",
                DiscoveryQaAdminRequests.QaSessionPageRequest.class);
        assertPostMapping(
                DiscoveryQaAdminController.class,
                "deleteSession",
                "session/delete",
                DiscoveryQaAdminRequests.QaSessionDeleteRequest.class);
        assertPostMapping(
                DiscoveryQaAdminController.class,
                "downloadSession",
                "session/download",
                DiscoveryQaAdminRequests.QaSessionExportRequest.class);
        assertPostMapping(DiscoveryQaAdminController.class, "getKnowledge", "knowledge/get");
        assertPostMapping(DiscoveryQaAdminController.class, "rebuildKnowledge", "knowledge/rebuild");
        assertPostMapping(
                DiscoveryQaAdminController.class,
                "updateKnowledge",
                "knowledge/update",
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
        assertHasPermission(
                DiscoveryQaAdminController.class,
                "downloadSession",
                "discovery:qa:view",
                DiscoveryQaAdminRequests.QaSessionExportRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        DiscoveryQaAdminRequests.QaSessionGetRequest sessionGetRequest = OBJECT_MAPPER.readValue(
                """
                        {"sessionId":"5001"}
                        """,
                DiscoveryQaAdminRequests.QaSessionGetRequest.class);
        assertEquals("5001", sessionGetRequest.getSessionId());
        assertJsonFields(sessionGetRequest, "sessionId");

        DiscoveryQaAdminRequests.QaSessionDeleteRequest sessionDeleteRequest = OBJECT_MAPPER.readValue(
                """
                        {"sessionId":"5001"}
                        """,
                DiscoveryQaAdminRequests.QaSessionDeleteRequest.class);
        assertEquals("5001", sessionDeleteRequest.getSessionId());
        assertJsonFields(sessionDeleteRequest, "sessionId");

        DiscoveryQaAdminRequests.QaSessionExportRequest sessionExportRequest = OBJECT_MAPPER.readValue(
                """
                        {"sessionId":"5001","requesterUserId":1001,"format":"CSV"}
                        """,
                DiscoveryQaAdminRequests.QaSessionExportRequest.class);
        assertEquals("5001", sessionExportRequest.getSessionId());
        assertEquals(1001L, sessionExportRequest.getRequesterUserId());
        assertEquals("CSV", sessionExportRequest.getFormat());
        assertJsonFields(sessionExportRequest, "sessionId", "requesterUserId", "format");

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

        DiscoveryQaAdminRequests.QaSessionPageRequest sessionPageRequest = OBJECT_MAPPER.readValue(
                """
                        {"title":"礼器","openedAtStart":"2026-01-01T00:00:00.000Z","openedAtEnd":"2026-01-31T23:59:59.999Z","pageNo":1,"pageSize":10}
                        """,
                DiscoveryQaAdminRequests.QaSessionPageRequest.class);
        assertEquals("礼器", sessionPageRequest.getTitle());
        assertEquals(1, sessionPageRequest.getPageNo());
        assertEquals(10, sessionPageRequest.getPageSize());
        assertJsonFields(sessionPageRequest, "title", "openedAtStart", "openedAtEnd", "pageNo", "pageSize");
    }

    @Test
    void sessionRequestsShouldRejectNonPositiveSessionId() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        DiscoveryQaAdminRequests.QaSessionGetRequest getRequest = new DiscoveryQaAdminRequests.QaSessionGetRequest();
        getRequest.setSessionId("0");
        DiscoveryQaAdminRequests.QaSessionDeleteRequest deleteRequest =
                new DiscoveryQaAdminRequests.QaSessionDeleteRequest();
        deleteRequest.setSessionId("-1");
        DiscoveryQaAdminRequests.QaSessionExportRequest exportRequest =
                new DiscoveryQaAdminRequests.QaSessionExportRequest();
        exportRequest.setSessionId("stored-5001");

        assertEquals(1, validator.validate(getRequest).size());
        assertEquals(1, validator.validate(deleteRequest).size());
        assertEquals(1, validator.validate(exportRequest).size());
    }

    @Test
    void endpointsShouldDelegateToApplicationService() {
        QaApplicationService qaService = mock(QaApplicationService.class);
        KnowledgeSyncApplicationService syncService = mock(KnowledgeSyncApplicationService.class);
        DiscoveryQaAdminController controller = new DiscoveryQaAdminController(qaService, syncService);

        when(qaService.getSessionDetail(any(QaSessionDetailQuery.class))).thenReturn(sampleSessionDetail());
        when(qaService.pageSessions(any(QaSessionQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 10, 1, List.of(sampleSession())));
        when(qaService.exportSession(any(ExportQaSessionCommand.class))).thenReturn(sampleExportResult());
        when(syncService.health())
                .thenReturn(new KnowledgeHealthResult(true, "fastgpt", null, Map.of("provider", "fastgpt")));
        when(syncService.rebuild()).thenReturn(9001L);
        when(syncService.syncContent(any(SyncKnowledgeContentCommand.class))).thenReturn(sampleSyncItem());
        when(syncService.pageSyncItems(any(KnowledgeSyncItemQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(2, 10, 1, List.of(sampleSyncItem())));

        DiscoveryQaAdminRequests.QaSessionGetRequest sessionRequest =
                new DiscoveryQaAdminRequests.QaSessionGetRequest();
        sessionRequest.setSessionId("5001");
        var sessionResponse = controller.getSession(sessionRequest);
        assertEquals("5001", sessionResponse.getId());
        assertEquals(1_718_000_200_000L, sessionResponse.getRemovedAt());

        DiscoveryQaAdminRequests.QaSessionPageRequest sessionPageRequest =
                new DiscoveryQaAdminRequests.QaSessionPageRequest();
        sessionPageRequest.setTitle("黄帝");
        sessionPageRequest.setOpenedAtStart(Instant.ofEpochMilli(1_718_000_000_000L));
        sessionPageRequest.setOpenedAtEnd(Instant.ofEpochMilli(1_718_086_400_000L));
        sessionPageRequest.setPageNo(1);
        sessionPageRequest.setPageSize(10);
        var sessionPageResponse = controller.pageSessions(sessionPageRequest);
        assertNotNull(sessionPageResponse);
        assertEquals(1, sessionPageResponse.getPageNo());
        assertEquals("5001", sessionPageResponse.getRecords().get(0).getId());
        assertEquals("黄帝问答", sessionPageResponse.getRecords().get(0).getTitle());

        DiscoveryQaAdminRequests.QaSessionDeleteRequest deleteRequest =
                new DiscoveryQaAdminRequests.QaSessionDeleteRequest();
        deleteRequest.setSessionId("5001");
        controller.deleteSession(deleteRequest);

        DiscoveryQaAdminRequests.QaSessionExportRequest exportRequest =
                new DiscoveryQaAdminRequests.QaSessionExportRequest();
        exportRequest.setSessionId("5001");
        exportRequest.setRequesterUserId(1001L);
        exportRequest.setFormat("CSV");
        var exportResponse = controller.downloadSession(exportRequest);
        assertEquals("7001", exportResponse.getId());
        assertEquals("8001", exportResponse.getStorageObjectId());
        assertEquals("SUCCEEDED", exportResponse.getExportStatus());
        assertEquals(1_718_000_000_000L, exportResponse.getRequestedAt());
        assertEquals(1_718_000_001_000L, exportResponse.getCompletedAt());
        assertEquals("discovery-qa-session-5001-7001.csv", exportResponse.getFilename());
        assertEquals("text/csv; charset=UTF-8", exportResponse.getContentType());

        var healthResponse = controller.getKnowledge();
        assertEquals("AVAILABLE", healthResponse.getStatus());
        assertEquals("fastgpt", healthResponse.getProvider());

        var rebuildBatchId = controller.rebuildKnowledge();
        assertEquals(9001L, rebuildBatchId);

        DiscoveryQaAdminRequests.KnowledgeSyncRequest syncRequest = new DiscoveryQaAdminRequests.KnowledgeSyncRequest();
        syncRequest.setContentType("SANCAI_ENTRY");
        syncRequest.setContentId(10001L);
        syncRequest.setCurrentVersionNo(3);
        var syncResponse = controller.updateKnowledge(syncRequest);
        assertNotNull(syncResponse);
        assertEquals("SANCAI_ENTRY", syncResponse.getContentType());
        assertEquals("黄帝", syncResponse.getTitle());
        assertEquals("SUCCEEDED", syncResponse.getSyncStatus());

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

        verify(qaService).getSessionDetail(argThat(query -> query != null && query.sessionId() == 5001L));
        ArgumentCaptor<QaSessionQuery> sessionQuery = ArgumentCaptor.forClass(QaSessionQuery.class);
        ArgumentCaptor<PageQuery> sessionPageQuery = ArgumentCaptor.forClass(PageQuery.class);
        verify(qaService).pageSessions(sessionQuery.capture(), sessionPageQuery.capture());
        assertEquals("黄帝", sessionQuery.getValue().title());
        assertEquals(1, sessionPageQuery.getValue().getPageNo());
        assertEquals(10, sessionPageQuery.getValue().getPageSize());
        ArgumentCaptor<DeleteQaSessionCommand> deleteCommand = ArgumentCaptor.forClass(DeleteQaSessionCommand.class);
        verify(qaService).deleteSession(deleteCommand.capture());
        assertEquals(5001L, deleteCommand.getValue().sessionId());
        assertEquals(Boolean.TRUE, deleteCommand.getValue().adminOperation());
        ArgumentCaptor<ExportQaSessionCommand> exportCommand = ArgumentCaptor.forClass(ExportQaSessionCommand.class);
        verify(qaService).exportSession(exportCommand.capture());
        assertEquals(5001L, exportCommand.getValue().sessionId());
        assertEquals(1001L, exportCommand.getValue().requesterUserId());
        assertEquals(Boolean.TRUE, exportCommand.getValue().adminOperation());
        assertEquals("CSV", exportCommand.getValue().format());
        verify(syncService).health();
        verify(syncService).rebuild();

        ArgumentCaptor<SyncKnowledgeContentCommand> syncContentCommand =
                ArgumentCaptor.forClass(SyncKnowledgeContentCommand.class);
        verify(syncService).syncContent(syncContentCommand.capture());
        assertEquals("SANCAI_ENTRY", syncContentCommand.getValue().contentType());
        assertEquals(10001L, syncContentCommand.getValue().contentId());
        assertEquals(3, syncContentCommand.getValue().currentVersionNo());

        ArgumentCaptor<KnowledgeSyncItemQuery> syncQuery = ArgumentCaptor.forClass(KnowledgeSyncItemQuery.class);
        ArgumentCaptor<PageQuery> syncPageQuery = ArgumentCaptor.forClass(PageQuery.class);
        verify(syncService).pageSyncItems(syncQuery.capture(), syncPageQuery.capture());
        assertEquals("SANCAI_ENTRY", syncQuery.getValue().contentType());
        assertEquals("FAILED", syncQuery.getValue().syncStatus());
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
                7001L, 5001L, "USER", "黄帝是谁", "SENT", 1, null, Instant.ofEpochMilli(1_718_000_050_000L), null)));
        return result;
    }

    private QaSessionResult sampleSession() {
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
                1_718_000_100_000L,
                null);
    }

    private QaSessionExportResult sampleExportResult() {
        return new QaSessionExportResult(
                7001L,
                5001L,
                "CSV",
                8001L,
                "SUCCEEDED",
                null,
                1_718_000_000_000L,
                1_718_000_001_000L,
                "discovery-qa-session-5001-7001.csv",
                "text/csv; charset=UTF-8");
    }

    private KnowledgeSyncItemResult sampleSyncItem() {
        return new KnowledgeSyncItemResult(
                "SANCAI_ENTRY:10001",
                "SANCAI_ENTRY",
                10001L,
                "黄帝",
                "kuzhambu-qa",
                3,
                "hash-xxx",
                "fastgpt",
                "kb-1",
                "item-1",
                "SUCCEEDED",
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
