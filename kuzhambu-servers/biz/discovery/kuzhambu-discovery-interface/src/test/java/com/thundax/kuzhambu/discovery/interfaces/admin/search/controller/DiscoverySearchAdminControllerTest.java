package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchLogResult;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchIndexRebuildRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchLogGetRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchLogPageRequest;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoverySearchAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminApiPaths() throws Exception {
        assertRequestMapping(DiscoverySearchAdminController.class, "/api/discovery/search-admin");
        assertPostMapping(
                DiscoverySearchAdminController.class, "pageLogs", "logs/page", DiscoverySearchLogPageRequest.class);
        assertPostMapping(
                DiscoverySearchAdminController.class, "getLog", "logs/get", DiscoverySearchLogGetRequest.class);
        assertPostMapping(
                DiscoverySearchAdminController.class,
                "rebuildIndex",
                "index/rebuild",
                DiscoverySearchIndexRebuildRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        DiscoverySearchLogPageRequest pageRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "queryText": "黄帝",
                  "intentTypes": ["ENTITY"],
                  "searchStatuses": ["SUCCEEDED"],
                  "operatorId": "user-1",
                  "dateFrom": "2026-01-01T00:00:00Z",
                  "dateTo": "2026-01-02T00:00:00Z",
                  "pageNo": 1,
                  "pageSize": 20
                }
                """,
                DiscoverySearchLogPageRequest.class);
        assertEquals("黄帝", pageRequest.getQueryText());
        assertJsonFields(
                pageRequest,
                "queryText",
                "intentTypes",
                "searchStatuses",
                "operatorId",
                "dateFrom",
                "dateTo",
                "pageNo",
                "pageSize");

        DiscoverySearchLogGetRequest getRequest = OBJECT_MAPPER.readValue(
                """
                {"searchLogId":"s-1"}
                """, DiscoverySearchLogGetRequest.class);
        assertEquals("s-1", getRequest.getSearchLogId());
        assertJsonFields(getRequest, "searchLogId");

        DiscoverySearchIndexRebuildRequest rebuildRequest = OBJECT_MAPPER.readValue(
                """
                {"confirm": true}
                """, DiscoverySearchIndexRebuildRequest.class);
        assertEquals(Boolean.TRUE, rebuildRequest.getConfirm());
        assertJsonFields(rebuildRequest, "confirm");
    }

    @Test
    void pageLogsShouldMapPageResponse() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchAdminController controller =
                new DiscoverySearchAdminController(service, searchIndexApplicationService);
        DiscoverySearchLogPageRequest request = new DiscoverySearchLogPageRequest();
        request.setPageNo(1);
        request.setPageSize(20);
        when(service.pageLogs(any()))
                .thenReturn(PageResult.of(
                        1,
                        20,
                        1,
                        List.of(new SearchLogResult(
                                "s-1",
                                "黄帝",
                                "黄帝",
                                "黄帝",
                                "ENTITY",
                                null,
                                1,
                                1,
                                "SUCCEEDED",
                                null,
                                null,
                                "user-1",
                                "req-1",
                                "trace-1",
                                1_718_000_000_000L,
                                List.of()))));

        var response = controller.pageLogs(request);

        verify(service).pageLogs(any());
        assertEquals(1, response.getRecords().size());
        assertEquals("s-1", response.getRecords().get(0).getSearchLogId());
    }

    @Test
    void getLogShouldMapDetailResponse() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchAdminController controller =
                new DiscoverySearchAdminController(service, searchIndexApplicationService);
        DiscoverySearchLogGetRequest request = new DiscoverySearchLogGetRequest();
        request.setSearchLogId("s-1");
        when(service.getLog("s-1"))
                .thenReturn(new SearchLogResult(
                        "s-1",
                        "黄帝",
                        "黄帝",
                        "黄帝",
                        "ENTITY",
                        "{\"knowledgeBases\":[\"SANCAI_ENTRY\"]}",
                        1,
                        1,
                        "SUCCEEDED",
                        null,
                        null,
                        "user-1",
                        "req-1",
                        "trace-1",
                        1_718_000_000_000L,
                        List.of()));

        var response = controller.getLog(request);

        verify(service).getLog("s-1");
        assertEquals("s-1", response.getSearchLogId());
        assertEquals("ENTITY", response.getIntentType());
        assertTrue(response.getSearchScopesJson().contains("SANCAI_ENTRY"));
    }

    @Test
    void rebuildIndexShouldDelegateToIndexApplicationService() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchAdminController controller =
                new DiscoverySearchAdminController(service, searchIndexApplicationService);
        DiscoverySearchIndexRebuildRequest request = new DiscoverySearchIndexRebuildRequest(Boolean.TRUE);
        when(searchIndexApplicationService.rebuildIndex()).thenReturn(12);

        Integer result = controller.rebuildIndex(request);

        verify(searchIndexApplicationService).rebuildIndex();
        assertEquals(12, result);
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
