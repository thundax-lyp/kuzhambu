package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.query.SearchAnalysisSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchAnalysisSummaryResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchLogResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchAnalysisSummaryRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchClickRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchIndexRebuildRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchLogGetRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchLogPageRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoverySearchAdminControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminApiPaths() throws Exception {
        assertRequestMapping(DiscoverySearchAdminQueryController.class, "/api/discovery/search");
        assertPostMapping(DiscoverySearchAdminQueryController.class, "search", "search", DiscoverySearchRequest.class);
        assertPostMapping(
                DiscoverySearchAdminQueryController.class, "click", "click", DiscoverySearchClickRequest.class);
        assertRequestMapping(DiscoverySearchAdminController.class, "/api/discovery/search-admin");
        assertPostMapping(
                DiscoverySearchAdminController.class, "pageLogs", "logs/page", DiscoverySearchLogPageRequest.class);
        assertPostMapping(
                DiscoverySearchAdminController.class, "getLog", "logs/get", DiscoverySearchLogGetRequest.class);
        assertPostMapping(
                DiscoverySearchAdminController.class,
                "getAnalysisSummary",
                "analysis/summary",
                DiscoverySearchAnalysisSummaryRequest.class);
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

        DiscoverySearchAnalysisSummaryRequest analysisRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "dateFrom": "2026-01-01T00:00:00Z",
                  "dateTo": "2026-01-02T00:00:00Z"
                }
                """,
                DiscoverySearchAnalysisSummaryRequest.class);
        assertEquals("2026-01-01T00:00:00Z", analysisRequest.getDateFrom());
        assertEquals("2026-01-02T00:00:00Z", analysisRequest.getDateTo());
        assertJsonFields(analysisRequest, "dateFrom", "dateTo");

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
    void searchShouldMapGroupedResults() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchAdminQueryController controller = new DiscoverySearchAdminQueryController(service);
        DiscoverySearchRequest request = new DiscoverySearchRequest();
        request.setQueryText("");
        request.setPageNo(1);
        request.setPageSize(20);
        when(service.search(any()))
                .thenReturn(new SearchLogResult(
                        "s-1",
                        "",
                        "",
                        "",
                        "KEYWORD_SEARCH",
                        null,
                        1,
                        1,
                        "SUCCEEDED",
                        null,
                        null,
                        "admin-1",
                        "req-1",
                        "trace-1",
                        1_718_000_000_000L,
                        List.of(new SearchGroupResult(
                                "SANCAI_ENTRY",
                                "三才图会",
                                1,
                                List.of(new SearchResult(
                                        "CLASSICS",
                                        "SANCAI_ENTRY",
                                        "1001",
                                        "黄帝",
                                        "上古帝王",
                                        null,
                                        1,
                                        1,
                                        "/classics/sancai/1001"))))));

        var response = controller.search(request);

        verify(service).search(any());
        assertEquals("s-1", response.getSearchLogId());
        assertEquals(1, response.getTotalCount());
        assertEquals("1001", response.getGroups().get(0).getItems().get(0).getContentId());
    }

    @Test
    void clickShouldDelegateToSearchApplicationService() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchAdminQueryController controller = new DiscoverySearchAdminQueryController(service);
        DiscoverySearchClickRequest request = new DiscoverySearchClickRequest();
        request.setSearchLogId("s-1");
        request.setContentDomain("CLASSICS");
        request.setContentType("SANCAI_ENTRY");
        request.setContentId("1001");
        request.setContentTitle("黄帝");
        request.setResultGroupKey("SANCAI_ENTRY");
        request.setResultRank(1);
        request.setGroupRank(1);
        request.setTargetPath("/classics/sancai/1001");
        when(service.recordClick(any())).thenReturn(Boolean.TRUE);

        Boolean result = controller.click(request);

        verify(service).recordClick(any());
        assertEquals(Boolean.TRUE, result);
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
        assertEquals("req-1", response.getRequestId());
        assertEquals("trace-1", response.getTraceId());
    }

    @Test
    void getLogShouldExposeFailureFieldsFromStoredLog() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchAdminController controller =
                new DiscoverySearchAdminController(service, searchIndexApplicationService);
        DiscoverySearchLogGetRequest request = new DiscoverySearchLogGetRequest();
        request.setSearchLogId("s-2");
        when(service.getLog("s-2"))
                .thenReturn(new SearchLogResult(
                        "s-2",
                        "黄帝",
                        "黄帝",
                        "黄帝",
                        "KEYWORD_SEARCH",
                        "{\"knowledgeBases\":[\"SANCAI_ENTRY\"],\"visibilityScopes\":[\"PUBLIC\"]}",
                        0,
                        0,
                        "FAILED",
                        "DISCOVERY-20001",
                        "Search backend is not implemented",
                        "user-2",
                        "req-2",
                        "trace-2",
                        1_718_000_100_000L,
                        List.of()));

        var response = controller.getLog(request);

        verify(service).getLog("s-2");
        assertEquals("FAILED", response.getSearchStatus());
        assertEquals("DISCOVERY-20001", response.getFailureCode());
        assertEquals("Search backend is not implemented", response.getFailureMessage());
        assertTrue(response.getSearchScopesJson().contains("visibilityScopes"));
        assertEquals("req-2", response.getRequestId());
        assertEquals("trace-2", response.getTraceId());
    }

    @Test
    void getAnalysisSummaryShouldMapFixedResponseFields() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchAdminController controller =
                new DiscoverySearchAdminController(service, searchIndexApplicationService);
        DiscoverySearchAnalysisSummaryRequest request = new DiscoverySearchAnalysisSummaryRequest();
        request.setDateFrom("2026-01-01T00:00:00Z");
        request.setDateTo("2026-01-02T00:00:00Z");
        when(service.getAnalysisSummary(any()))
                .thenReturn(new SearchAnalysisSummaryResult(
                        12L, 2L, 3L, 9L, List.of(new SearchAnalysisSummaryResult.TopQuery("黄帝", 5L))));

        var response = controller.getAnalysisSummary(request);

        ArgumentCaptor<SearchAnalysisSummaryQuery> queryCaptor =
                ArgumentCaptor.forClass(SearchAnalysisSummaryQuery.class);
        verify(service).getAnalysisSummary(queryCaptor.capture());
        assertEquals(new Date(1_767_225_600_000L), queryCaptor.getValue().getDateFrom());
        assertEquals(new Date(1_767_312_000_000L), queryCaptor.getValue().getDateTo());
        assertEquals(12L, response.getSearchCount());
        assertEquals(2L, response.getFailedSearchCount());
        assertEquals(3L, response.getZeroResultSearchCount());
        assertEquals(9L, response.getClickCount());
        assertEquals("黄帝", response.getTopQueries().get(0).getQueryText());
        assertEquals(5L, response.getTopQueries().get(0).getCount());
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
