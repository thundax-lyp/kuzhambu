package com.thundax.kuzhambu.discovery.application.search.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchAnalysisSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchLogPageQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.DefaultSearchPermissionFilter;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchLog;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickRepository;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchLogRepository;
import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SearchApplicationServiceImplTest {

    @AfterEach
    void clearContext() {
        KuzhambuContextHolder.clear();
    }

    @Test
    void searchShouldTranslateBackendNotImplementedToBizException() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                "黄帝",
                List.of("SANCAI_ENTRY"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                1,
                20,
                "ANONYMOUS",
                null,
                null,
                null);
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(
                        new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenThrow(new UnsupportedOperationException("not ready"));

        BizException exception = assertThrows(BizException.class, () -> service.search(query));
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);

        assertEquals("DISCOVERY-20001", exception.getCode());
        verify(searchLogRepository).save(searchLogCaptor.capture());
        assertEquals("FAILED", searchLogCaptor.getValue().getSearchStatus());
        assertEquals("DISCOVERY-20001", searchLogCaptor.getValue().getFailureCode());
    }

    @Test
    void searchShouldReturnGroupedResultsWhenGatewaySucceeds() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(new SearchGroupResult(
                        "SANCAI_ENTRY",
                        "三才图会",
                        1,
                        List.of(new SearchResult(
                                "CLASSICS",
                                "SANCAI_ENTRY",
                                "1001",
                                "黄帝",
                                "上古帝王",
                                "<mark>黄帝</mark>上古帝王",
                                1,
                                1,
                                "/classics/sancai/1001")))));
        SearchQuery query = new SearchQuery(
                "黄帝",
                List.of("SANCAI_ENTRY"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                1,
                20,
                "ANONYMOUS",
                null,
                "req-1",
                "trace-1");
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult(
                        "黄帝", "黄帝 传说", "NATURAL_LANGUAGE_SEARCH", List.of("轩辕"), List.of(), "req-1", "trace-1"));

        var result = service.search(query);
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);

        verify(searchLogRepository).save(searchLogCaptor.capture());
        assertEquals(1, result.getGroups().size());
        assertEquals(1, result.getResultTotalCount());
        assertEquals("黄帝 传说", result.getDisplayQueryText());
        assertEquals("NATURAL_LANGUAGE_SEARCH", result.getIntentType());
        assertEquals("SUCCEEDED", searchLogCaptor.getValue().getSearchStatus());
        assertEquals("黄帝 传说", searchLogCaptor.getValue().getDisplayQueryText());
        assertEquals(searchLogCaptor.getValue().getSearchLogId(), result.getSearchLogId());
        assertTrue(result.getSearchScopesJson().contains("SANCAI_ENTRY"));
        assertEquals(
                "<mark>黄帝</mark>上古帝王",
                result.getGroups().get(0).getItems().get(0).getHighlightText());
    }

    @Test
    void searchShouldFilterPrivateResultsForAnonymousOperator() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                "黄帝",
                List.of("SANCAI_ENTRY"),
                List.of("11"),
                List.of("上古"),
                List.of("PUBLISHED"),
                List.of("PUBLIC", "PRIVATE"),
                null,
                null,
                1,
                20,
                "ANONYMOUS",
                null,
                "req-3",
                "trace-3");
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(
                        new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(new SearchGroupResult(
                        "SANCAI_ENTRY",
                        "三才图会",
                        2,
                        List.of(
                                searchResult("SANCAI_ENTRY", "1001", "PUBLIC"),
                                searchResult("SANCAI_ENTRY", "1002", "PRIVATE")))));

        var result = service.search(query);

        assertEquals(1, result.getResultTotalCount());
        assertEquals(1, result.getGroupTotalCount());
        assertEquals(1, result.getGroups().get(0).getCount());
        assertEquals("1001", result.getGroups().get(0).getItems().get(0).getContentId());
    }

    @Test
    void searchShouldKeepPrivateResultsWhenSubjectHasContentPermission() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "admin-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Set.of("classics:sancai:view")));
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                "黄帝",
                List.of("SANCAI_ENTRY"),
                List.of(),
                List.of(),
                List.of(),
                List.of("PRIVATE"),
                null,
                null,
                1,
                20,
                "ADMIN",
                "admin-1",
                "req-4",
                "trace-4");
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(
                        new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(new SearchGroupResult(
                        "SANCAI_ENTRY", "三才图会", 1, List.of(searchResult("SANCAI_ENTRY", "1002", "PRIVATE")))));

        var result = service.search(query);

        assertEquals(1, result.getResultTotalCount());
        assertEquals("1002", result.getGroups().get(0).getItems().get(0).getContentId());
    }

    @Test
    void searchShouldRejectUnknownPrivateContentType() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "admin-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Set.of("classics:content:view")));
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                "黄帝",
                List.of("UNKNOWN_CONTENT"),
                List.of(),
                List.of(),
                List.of(),
                List.of("PRIVATE"),
                null,
                null,
                1,
                20,
                "ADMIN",
                "admin-1",
                "req-5",
                "trace-5");
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(
                        new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(new SearchGroupResult(
                        "UNKNOWN_CONTENT", "未知内容", 1, List.of(searchResult("UNKNOWN_CONTENT", "1003", "PRIVATE")))));

        var result = service.search(query);

        assertEquals(0, result.getResultTotalCount());
        assertEquals(0, result.getGroupTotalCount());
        assertTrue(result.getGroups().isEmpty());
    }

    @Test
    void recordClickShouldPersistCommandPayload() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        when(searchLogRepository.getBySearchLogId("s-1"))
                .thenReturn(new SearchLog(
                        1L,
                        "s-1",
                        "黄帝",
                        "黄帝",
                        "黄帝",
                        SearchIntentType.KEYWORD_SEARCH,
                        null,
                        1,
                        1,
                        "SUCCEEDED",
                        null,
                        null,
                        "ANONYMOUS",
                        null,
                        null,
                        null,
                        new Date()));

        Boolean result = service.recordClick(new SearchClickCreateCommand(
                "s-1",
                "CLASSICS",
                "SANCAI_ENTRY",
                "1001",
                "黄帝",
                "SANCAI_ENTRY",
                1,
                1,
                "/classics/sancai/1001",
                "ANONYMOUS",
                null,
                null,
                null));

        verify(searchClickRepository).save(any());
        assertTrue(result);
    }

    @Test
    void recordClickShouldRejectUnknownSearchLogId() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        when(searchLogRepository.getBySearchLogId("missing")).thenReturn(null);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.recordClick(new SearchClickCreateCommand(
                        "missing",
                        "CLASSICS",
                        "SANCAI_ENTRY",
                        "1001",
                        "黄帝",
                        "SANCAI_ENTRY",
                        1,
                        1,
                        "/classics/sancai/1001",
                        "ANONYMOUS",
                        null,
                        null,
                        null)));

        assertEquals("DISCOVERY-20002", exception.getCode());
    }

    @Test
    void pageLogsShouldUseFirstIntentAndStatusFilter() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        when(searchLogRepository.page("黄帝", "ENTITY", "SUCCEEDED", "user-1", 1, 20))
                .thenReturn(PageResult.of(
                        1,
                        20,
                        1,
                        List.of(new SearchLog(
                                1L,
                                "s-1",
                                "黄帝",
                                "黄帝",
                                "黄帝",
                                SearchIntentType.KEYWORD_SEARCH,
                                null,
                                1,
                                1,
                                "SUCCEEDED",
                                null,
                                null,
                                "USER",
                                "user-1",
                                "req-1",
                                "trace-1",
                                new Date(1_718_000_000_000L)))));

        var result = service.pageLogs(new SearchLogPageQuery(
                "黄帝", List.of("ENTITY", "KEYWORD"), List.of("SUCCEEDED", "FAILED"), "user-1", null, null, 1, 20));

        verify(searchLogRepository).page("黄帝", "ENTITY", "SUCCEEDED", "user-1", 1, 20);
        assertEquals(1, result.getRecords().size());
        assertEquals("s-1", result.getRecords().get(0).getSearchLogId());
    }

    @Test
    void getAnalysisSummaryShouldAggregateSearchLogsAndClicks() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        Date dateFrom = new Date(1_718_000_000_000L);
        Date dateTo = new Date(1_720_419_200_000L);
        when(searchLogRepository.listByCreatedAtRange(dateFrom, dateTo))
                .thenReturn(List.of(
                        searchLog("黄帝", "SUCCEEDED", 3),
                        searchLog("黄帝", "SUCCEEDED", 0),
                        searchLog("天文", "FAILED", 0),
                        searchLog("地理", "SUCCEEDED", 1),
                        searchLog("地理", "SUCCEEDED", 2),
                        searchLog("礼制", "SUCCEEDED", 1)));
        when(searchClickRepository.countByCreatedAtRange(dateFrom, dateTo)).thenReturn(7L);

        var result = service.getAnalysisSummary(new SearchAnalysisSummaryQuery(dateFrom, dateTo));

        assertEquals(6L, result.getSearchCount());
        assertEquals(1L, result.getFailedSearchCount());
        assertEquals(1L, result.getZeroResultSearchCount());
        assertEquals(7L, result.getClickCount());
        assertEquals(4, result.getTopQueries().size());
        assertEquals("地理", result.getTopQueries().get(0).getQueryText());
        assertEquals(2L, result.getTopQueries().get(0).getCount());
        assertEquals("黄帝", result.getTopQueries().get(1).getQueryText());
        assertEquals("礼制", result.getTopQueries().get(3).getQueryText());
    }

    @Test
    void getAnalysisSummaryShouldLimitTopQueriesToTen() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                mock(SearchIndexGateway.class),
                new DefaultSearchPermissionFilter(),
                mock(QueryUnderstandingApplicationService.class));
        when(searchLogRepository.listByCreatedAtRange(null, null))
                .thenReturn(List.of(
                        searchLog("q01", "SUCCEEDED", 1),
                        searchLog("q02", "SUCCEEDED", 1),
                        searchLog("q03", "SUCCEEDED", 1),
                        searchLog("q04", "SUCCEEDED", 1),
                        searchLog("q05", "SUCCEEDED", 1),
                        searchLog("q06", "SUCCEEDED", 1),
                        searchLog("q07", "SUCCEEDED", 1),
                        searchLog("q08", "SUCCEEDED", 1),
                        searchLog("q09", "SUCCEEDED", 1),
                        searchLog("q10", "SUCCEEDED", 1),
                        searchLog("q11", "SUCCEEDED", 1)));

        var result = service.getAnalysisSummary(new SearchAnalysisSummaryQuery(null, null));

        assertEquals(10, result.getTopQueries().size());
        assertEquals("q01", result.getTopQueries().get(0).getQueryText());
        assertEquals("q10", result.getTopQueries().get(9).getQueryText());
    }

    @Test
    void searchShouldPersistFailureLogAndRethrowBizException() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter(),
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                "黄帝",
                List.of("SANCAI_ENTRY"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                1,
                20,
                "ANONYMOUS",
                "user-1",
                "req-2",
                "trace-2");
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult(
                        "黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), List.of(), "req-2", "trace-2"));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenThrow(new BizException("DISCOVERY-29999", "discovery.search.test", "boom"));

        BizException exception = assertThrows(BizException.class, () -> service.search(query));
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);

        assertEquals("DISCOVERY-29999", exception.getCode());
        verify(searchLogRepository, times(1)).save(searchLogCaptor.capture());
        assertEquals("FAILED", searchLogCaptor.getValue().getSearchStatus());
        assertEquals("DISCOVERY-29999", searchLogCaptor.getValue().getFailureCode());
        assertEquals("boom", searchLogCaptor.getValue().getFailureMessage());
    }

    private SearchLog searchLog(String queryText, String searchStatus, Integer resultTotalCount) {
        return new SearchLog(
                1L,
                "s-" + queryText + "-" + searchStatus + "-" + resultTotalCount,
                queryText,
                queryText,
                queryText,
                SearchIntentType.KEYWORD_SEARCH,
                null,
                resultTotalCount,
                resultTotalCount == null ? 0 : Math.min(resultTotalCount, 1),
                searchStatus,
                null,
                null,
                "ANONYMOUS",
                null,
                null,
                null,
                new Date());
    }

    private SearchResult searchResult(String contentType, String contentId, String visibility) {
        return new SearchResult(
                "CLASSICS",
                contentType,
                contentId,
                contentType,
                "11",
                "黄帝",
                "上古帝王",
                "<mark>黄帝</mark>上古帝王",
                List.of("上古"),
                "PUBLISHED",
                visibility,
                1_718_000_000_000L,
                1,
                1,
                "/classics/sancai/" + contentId);
    }
}
