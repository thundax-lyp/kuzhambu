package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.query.SearchEventQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchStatisticsSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchEventResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchStatisticsSummaryResult;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexApplicationService;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchClickEventRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchEventGetRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchEventPageRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchIndexRebuildRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchPreviewRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchStatisticsSummaryRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiscoverySearchStatisticsControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void routesShouldKeepAdminApiPaths() throws Exception {
        assertRequestMapping(DiscoverySearchStatisticsQueryController.class, "/api/discovery/search");
        assertPostMapping(
                DiscoverySearchStatisticsQueryController.class, "search", "search", DiscoverySearchRequest.class);
        assertPostMapping(
                DiscoverySearchStatisticsQueryController.class,
                "preview",
                "preview",
                DiscoverySearchPreviewRequest.class);
        assertPostMapping(
                DiscoverySearchStatisticsQueryController.class,
                "click",
                "click",
                DiscoverySearchClickEventRequest.class);
        assertRequestMapping(DiscoverySearchStatisticsController.class, "/api/discovery/search-statistics");
        assertPostMapping(
                DiscoverySearchStatisticsController.class,
                "pageEvents",
                "events/page",
                DiscoverySearchEventPageRequest.class);
        assertPostMapping(
                DiscoverySearchStatisticsController.class,
                "getEvent",
                "events/get",
                DiscoverySearchEventGetRequest.class);
        assertPostMapping(
                DiscoverySearchStatisticsController.class,
                "getStatisticsSummary",
                "summary",
                DiscoverySearchStatisticsSummaryRequest.class);
        assertPostMapping(
                DiscoverySearchStatisticsController.class,
                "rebuildIndex",
                "index/rebuild",
                DiscoverySearchIndexRebuildRequest.class);
    }

    @Test
    void requestAndResponseJsonFieldsShouldRemainStable() throws Exception {
        DiscoverySearchEventPageRequest pageRequest = OBJECT_MAPPER.readValue(
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
                DiscoverySearchEventPageRequest.class);
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

        DiscoverySearchEventGetRequest getRequest = OBJECT_MAPPER.readValue(
                """
                {"id":"1"}
                """, DiscoverySearchEventGetRequest.class);
        assertEquals("1", getRequest.getId());
        assertJsonFields(getRequest, "id");

        DiscoverySearchStatisticsSummaryRequest analysisRequest = OBJECT_MAPPER.readValue(
                """
                {
                  "dateFrom": "2026-01-01T00:00:00Z",
                  "dateTo": "2026-01-02T00:00:00Z"
                }
                """,
                DiscoverySearchStatisticsSummaryRequest.class);
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
    void pageEventsShouldMapPageResponse() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchStatisticsController controller =
                new DiscoverySearchStatisticsController(service, searchIndexApplicationService);
        DiscoverySearchEventPageRequest request = new DiscoverySearchEventPageRequest();
        request.setPageNo(1);
        request.setPageSize(20);
        when(service.pageEvents(any(), any()))
                .thenReturn(PageResult.of(
                        1,
                        20,
                        1,
                        List.of(new SearchEventResult(
                                SearchEventIdCodec.toDomain(1L),
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

        var response = controller.pageEvents(request);

        verify(service).pageEvents(any(), any());
        assertEquals(1, response.getRecords().size());
        assertEquals("1", response.getRecords().get(0).getId());
    }

    @Test
    void searchShouldMapGroupedResults() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchStatisticsQueryController controller = new DiscoverySearchStatisticsQueryController(service);
        DiscoverySearchRequest request = new DiscoverySearchRequest();
        request.setQueryText("辞官");
        request.setPageNo(1);
        request.setPageSize(20);
        when(service.search(any(), any()))
                .thenReturn(new SearchEventResult(
                        SearchEventIdCodec.toDomain(1L),
                        "辞官",
                        "辞官",
                        "辞官",
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
        ArgumentCaptor<SearchQuery> queryCaptor = ArgumentCaptor.forClass(SearchQuery.class);

        verify(service).search(queryCaptor.capture(), any());
        assertEquals("辞官", queryCaptor.getValue().getQueryText());
        assertEquals("ADMIN", queryCaptor.getValue().getOperatorType());
        assertTrue(queryCaptor.getValue().getRequestId() != null);
        assertTrue(queryCaptor.getValue().getTraceId() != null);
        assertEquals("1", response.getId());
        assertEquals(1, response.getTotalCount());
        assertEquals("1001", response.getGroups().get(0).getItems().get(0).getContentId());
    }

    @Test
    void clickShouldDelegateToSearchApplicationService() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchStatisticsQueryController controller = new DiscoverySearchStatisticsQueryController(service);
        DiscoverySearchClickEventRequest request = new DiscoverySearchClickEventRequest();
        request.setSearchEventId("1");
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
    void clickRequestShouldRejectNonnumericSearchEventId() {
        DiscoverySearchClickEventRequest request = new DiscoverySearchClickEventRequest();
        request.setSearchEventId("EVT-1001");
        request.setContentDomain("CLASSICS");
        request.setContentType("SANCAI_ENTRY");
        request.setContentId("1001");
        request.setResultGroupKey("SANCAI_ENTRY");
        request.setResultRank(1);
        request.setGroupRank(1);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void getRequestShouldRejectNonPositiveSearchEventId() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        DiscoverySearchEventGetRequest zeroRequest = new DiscoverySearchEventGetRequest();
        zeroRequest.setId("0");
        DiscoverySearchEventGetRequest negativeRequest = new DiscoverySearchEventGetRequest();
        negativeRequest.setId("-1");

        assertFalse(validator.validate(zeroRequest).isEmpty());
        assertFalse(validator.validate(negativeRequest).isEmpty());
    }

    @Test
    void previewShouldMapSearchIndexDocumentFields() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        DiscoverySearchStatisticsQueryController controller = new DiscoverySearchStatisticsQueryController(service);
        DiscoverySearchPreviewRequest request = new DiscoverySearchPreviewRequest();
        request.setContentType("SANCAI_ENTRY");
        request.setContentId("1001");
        when(service.getPreview(any()))
                .thenReturn(new SearchPreviewResult(
                        "CLASSICS",
                        "SANCAI_ENTRY",
                        "1001",
                        "SANCAI_ENTRY",
                        "11",
                        "天文",
                        "黄帝",
                        "摘要",
                        "正文",
                        List.of("上古"),
                        3,
                        1_767_225_600_000L,
                        1_767_312_000_000L,
                        "/classics/sancai/1001"));

        var response = controller.preview(request);

        verify(service)
                .getPreview(argThat(
                        query -> "SANCAI_ENTRY".equals(query.getContentType()) && "1001".equals(query.getContentId())));
        assertEquals("黄帝", response.getTitle());
        assertEquals("正文", response.getBodyText());
        assertEquals("/classics/sancai/1001", response.getTargetPath());
    }

    @Test
    void getEventShouldMapDetailResponse() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchStatisticsController controller =
                new DiscoverySearchStatisticsController(service, searchIndexApplicationService);
        DiscoverySearchEventGetRequest request = new DiscoverySearchEventGetRequest();
        request.setId("1");
        SearchEventQuery eventQuery = new SearchEventQuery(1L, null, null, null, null, null, null);
        when(service.getEvent(eventQuery))
                .thenReturn(new SearchEventResult(
                        SearchEventIdCodec.toDomain(1L),
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

        var response = controller.getEvent(request);

        verify(service).getEvent(eventQuery);
        assertEquals("1", response.getId());
        assertEquals("ENTITY", response.getIntentType());
        assertTrue(response.getSearchScopesJson().contains("SANCAI_ENTRY"));
        assertEquals("req-1", response.getRequestId());
        assertEquals("trace-1", response.getTraceId());
    }

    @Test
    void getEventShouldExposeFailureFieldsFromStoredLog() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchStatisticsController controller =
                new DiscoverySearchStatisticsController(service, searchIndexApplicationService);
        DiscoverySearchEventGetRequest request = new DiscoverySearchEventGetRequest();
        request.setId("2");
        SearchEventQuery eventQuery2 = new SearchEventQuery(2L, null, null, null, null, null, null);
        when(service.getEvent(eventQuery2))
                .thenReturn(new SearchEventResult(
                        SearchEventIdCodec.toDomain(2L),
                        "黄帝",
                        "黄帝",
                        "黄帝",
                        "KEYWORD_SEARCH",
                        "{\"knowledgeBases\":[\"SANCAI_ENTRY\"]}",
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

        var response = controller.getEvent(request);

        verify(service).getEvent(eventQuery2);
        assertEquals("FAILED", response.getSearchStatus());
        assertEquals("DISCOVERY-20001", response.getFailureCode());
        assertEquals("Search backend is not implemented", response.getFailureMessage());
        assertTrue(response.getSearchScopesJson().contains("knowledgeBases"));
        assertEquals("req-2", response.getRequestId());
        assertEquals("trace-2", response.getTraceId());
    }

    @Test
    void getStatisticsSummaryShouldMapFixedResponseFields() {
        SearchApplicationService service = mock(SearchApplicationService.class);
        SearchIndexApplicationService searchIndexApplicationService = mock(SearchIndexApplicationService.class);
        DiscoverySearchStatisticsController controller =
                new DiscoverySearchStatisticsController(service, searchIndexApplicationService);
        DiscoverySearchStatisticsSummaryRequest request = new DiscoverySearchStatisticsSummaryRequest();
        request.setDateFrom("2026-01-01T00:00:00Z");
        request.setDateTo("2026-01-02T00:00:00Z");
        when(service.getStatisticsSummary(any()))
                .thenReturn(new SearchStatisticsSummaryResult(
                        12L, 2L, 3L, 9L, List.of(new SearchStatisticsSummaryResult.TopQueryItem("黄帝", 5L))));

        var response = controller.getStatisticsSummary(request);

        ArgumentCaptor<SearchStatisticsSummaryQuery> queryCaptor =
                ArgumentCaptor.forClass(SearchStatisticsSummaryQuery.class);
        verify(service).getStatisticsSummary(queryCaptor.capture());
        assertEquals(
                Instant.ofEpochMilli(1_767_225_600_000L), queryCaptor.getValue().getDateFrom());
        assertEquals(
                Instant.ofEpochMilli(1_767_312_000_000L), queryCaptor.getValue().getDateTo());
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
        DiscoverySearchStatisticsController controller =
                new DiscoverySearchStatisticsController(service, searchIndexApplicationService);
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
