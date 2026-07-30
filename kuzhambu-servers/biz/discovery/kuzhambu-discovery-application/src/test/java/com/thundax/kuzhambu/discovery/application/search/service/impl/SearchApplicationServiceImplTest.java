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
import com.thundax.kuzhambu.common.core.traceability.codec.RequestIdCodec;
import com.thundax.kuzhambu.common.core.traceability.codec.TraceIdCodec;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickEventCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchEventPageQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPreviewQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchStatisticsSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPageResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchKeyword;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchScope;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickEventRepository;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchEventRepository;
import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import java.time.Instant;
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
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
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
                .thenReturn(new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenThrow(new UnsupportedOperationException("not ready"));

        BizException exception = assertThrows(BizException.class, () -> service.search(query));
        ArgumentCaptor<SearchEvent> searchEventCaptor = ArgumentCaptor.forClass(SearchEvent.class);

        assertEquals("DISCOVERY-20001", exception.getCode());
        verify(searchEventRepository).save(searchEventCaptor.capture());
        assertEquals("FAILED", searchEventCaptor.getValue().getSearchStatus());
        assertEquals("DISCOVERY-20001", searchEventCaptor.getValue().getFailureCode());
        assertTrue(searchEventCaptor.getValue().getSearchLatencyMs() >= 0);
    }

    @Test
    void searchShouldReturnGroupedResultsWhenGatewaySucceeds() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(searchPageResult(
                        1,
                        new SearchGroupResult(
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
        Instant dateFrom = Instant.ofEpochMilli(1_718_000_000_000L);
        Instant dateTo = Instant.ofEpochMilli(1_720_419_200_000L);
        SearchQuery query = new SearchQuery(
                "黄帝",
                List.of("SANCAI_ENTRY"),
                List.of("11"),
                List.of("上古"),
                List.of("PUBLISHED"),
                List.of("PUBLIC"),
                dateFrom,
                dateTo,
                1,
                20,
                "ANONYMOUS",
                null,
                requestId("req-1"),
                traceId("trace-1"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult(
                        "黄帝",
                        "黄帝 传说",
                        "NATURAL_LANGUAGE_SEARCH",
                        List.of(new QueryUnderstandingResult.RecognizedEntityResult("轩辕", "ENTITY", "轩辕")),
                        "req-1",
                        "trace-1"));

        var result = service.search(query);
        ArgumentCaptor<SearchEvent> searchEventCaptor = ArgumentCaptor.forClass(SearchEvent.class);

        verify(searchEventRepository).save(searchEventCaptor.capture());
        assertEquals(1, result.getGroups().size());
        assertEquals(1, result.getResultTotalCount());
        assertEquals("黄帝 传说", result.getDisplayQueryText());
        assertEquals("NATURAL_LANGUAGE_SEARCH", result.getIntentType());
        assertEquals("SUCCEEDED", searchEventCaptor.getValue().getSearchStatus());
        assertEquals("黄帝 传说", searchEventCaptor.getValue().getDisplayQueryText());
        assertTrue(searchEventCaptor.getValue().getSearchLatencyMs() >= 0);
        assertEquals(searchEventCaptor.getValue().getId(), result.getId());
        assertTrue(result.getSearchScopesJson().contains("SANCAI_ENTRY"));
        assertTrue(result.getSearchScopesJson().contains("\"categoryCodes\":[\"11\"]"));
        assertTrue(result.getSearchScopesJson().contains("\"tagNames\":[\"上古\"]"));
        assertTrue(result.getSearchScopesJson().contains("\"contentStatuses\":[\"PUBLISHED\"]"));
        assertTrue(result.getSearchScopesJson().contains("\"visibilityScopes\":[\"PUBLIC\"]"));
        assertTrue(result.getSearchScopesJson().contains("\"dateFrom\":1718000000000"));
        assertTrue(result.getSearchScopesJson().contains("\"dateTo\":1720419200000"));
        assertEquals(
                "<mark>黄帝</mark>上古帝王",
                result.getGroups().get(0).getItems().get(0).getHighlightText());
    }

    @Test
    void searchShouldAllowBlankQueryForDefaultPublishedContentList() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                "",
                List.of(),
                List.of(),
                List.of(),
                List.of("PUBLISHED"),
                List.of("PUBLIC"),
                null,
                null,
                1,
                20,
                "ADMIN",
                "admin-1",
                requestId("req-blank"),
                traceId("trace-blank"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(
                        new QueryUnderstandingResult("", "", "KEYWORD_SEARCH", List.of(), "req-blank", "trace-blank"));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(searchPageResult(
                        20,
                        new SearchGroupResult(
                                "SANCAI_ENTRY", "三才图会", 1, List.of(searchResult("SANCAI_ENTRY", "1001", "PUBLIC")))));

        var result = service.search(query);
        ArgumentCaptor<SearchKeyword> keywordCaptor = ArgumentCaptor.forClass(SearchKeyword.class);
        ArgumentCaptor<SearchEvent> searchEventCaptor = ArgumentCaptor.forClass(SearchEvent.class);

        verify(searchIndexGateway).search(keywordCaptor.capture(), any(), any(Integer.class), any(Integer.class));
        verify(searchEventRepository).save(searchEventCaptor.capture());
        assertEquals("", keywordCaptor.getValue().getNormalizedText());
        assertEquals(20, result.getResultTotalCount());
        assertEquals(20, searchEventCaptor.getValue().getResultTotalCount());
        assertEquals("SUCCEEDED", searchEventCaptor.getValue().getSearchStatus());
        assertEquals("", searchEventCaptor.getValue().getNormalizedQueryText());
    }

    @Test
    void searchShouldNormalizeNullQueryTextToBlankSearch() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                null,
                List.of(),
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
                requestId("req-null-query"),
                traceId("trace-null-query"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult(
                        "", "", "KEYWORD_SEARCH", List.of(), "req-null-query", "trace-null-query"));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(searchPageResult(0));
        ArgumentCaptor<SearchKeyword> keywordCaptor = ArgumentCaptor.forClass(SearchKeyword.class);
        ArgumentCaptor<SearchEvent> searchEventCaptor = ArgumentCaptor.forClass(SearchEvent.class);

        var result = service.search(query);

        verify(searchIndexGateway).search(keywordCaptor.capture(), any(), any(Integer.class), any(Integer.class));
        verify(searchEventRepository).save(searchEventCaptor.capture());
        assertEquals("", query.getQueryText());
        assertEquals("", keywordCaptor.getValue().getRawText());
        assertEquals("", result.getQueryText());
        assertEquals("", searchEventCaptor.getValue().getQueryText());
        assertEquals("SUCCEEDED", searchEventCaptor.getValue().getSearchStatus());
    }

    @Test
    void searchShouldForcePublicScopeForAnonymousOperator() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
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
                requestId("req-3"),
                traceId("trace-3"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(searchPageResult(
                        1,
                        new SearchGroupResult(
                                "SANCAI_ENTRY", "三才图会", 1, List.of(searchResult("SANCAI_ENTRY", "1001", "PUBLIC")))));

        var result = service.search(query);

        assertEquals(1, result.getResultTotalCount());
        assertEquals(1, result.getGroupTotalCount());
        assertEquals(1, result.getGroups().get(0).getCount());
        assertEquals("1001", result.getGroups().get(0).getItems().get(0).getContentId());
        ArgumentCaptor<SearchScope> scopeCaptor = ArgumentCaptor.forClass(SearchScope.class);
        verify(searchIndexGateway).search(any(), scopeCaptor.capture(), any(Integer.class), any(Integer.class));
        assertEquals(List.of("PUBLIC"), scopeCaptor.getValue().getVisibilityScopes());
        assertTrue(scopeCaptor.getValue().getPrivateKnowledgeBases().isEmpty());
    }

    @Test
    void searchShouldPushPermissionScopeToGatewayBeforePagination() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "admin-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Set.of("classics:sancai:view")));
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                "黄帝",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of("PUBLIC", "PRIVATE"),
                null,
                null,
                1,
                2,
                "ADMIN",
                "admin-1",
                requestId("req-total"),
                traceId("trace-total"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(searchPageResult(
                        2,
                        new SearchGroupResult(
                                "SANCAI_ENTRY",
                                "三才图会",
                                2,
                                List.of(
                                        searchResult("SANCAI_ENTRY", "1001", "PUBLIC"),
                                        searchResult("SANCAI_ENTRY", "1002", "PRIVATE")))));

        var result = service.search(query);
        ArgumentCaptor<SearchScope> scopeCaptor = ArgumentCaptor.forClass(SearchScope.class);

        assertEquals(2, result.getResultTotalCount());
        assertEquals(1, result.getGroupTotalCount());
        assertEquals("1001", result.getGroups().get(0).getItems().get(0).getContentId());
        verify(searchIndexGateway, times(1))
                .search(any(), scopeCaptor.capture(), any(Integer.class), any(Integer.class));
        assertTrue(scopeCaptor.getValue().getPrivateKnowledgeBases().isEmpty());
    }

    @Test
    void searchShouldKeepPrivateResultsWhenSubjectHasContentPermission() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "admin-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Set.of("classics:sancai:view")));
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
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
                requestId("req-4"),
                traceId("trace-4"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(searchPageResult(
                        1,
                        new SearchGroupResult(
                                "SANCAI_ENTRY", "三才图会", 1, List.of(searchResult("SANCAI_ENTRY", "1002", "PRIVATE")))));

        var result = service.search(query);

        assertEquals(1, result.getResultTotalCount());
        assertEquals("1002", result.getGroups().get(0).getItems().get(0).getContentId());
    }

    @Test
    void searchShouldNotInferPrivateScopeFromFullContentPermission() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "admin-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Set.of("classics:content:view")));
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
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
                requestId("req-5"),
                traceId("trace-5"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), null, null));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(searchPageResult(0));

        var result = service.search(query);
        ArgumentCaptor<SearchScope> scopeCaptor = ArgumentCaptor.forClass(SearchScope.class);

        assertEquals(0, result.getResultTotalCount());
        assertEquals(0, result.getGroupTotalCount());
        assertTrue(result.getGroups().isEmpty());
        verify(searchIndexGateway).search(any(), scopeCaptor.capture(), any(Integer.class), any(Integer.class));
        assertTrue(scopeCaptor.getValue().getPrivateKnowledgeBases().isEmpty());
    }

    @Test
    void previewShouldUseSearchPermissionScope() {
        KuzhambuContextHolder.setSubject(new KuzhambuSubject(
                "admin-1", KuzhambuSubjectType.ADMIN_USER, "admin", "token-1", Set.of("classics:wangqi:view")));
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        SearchPreviewResult preview = new SearchPreviewResult();
        preview.setContentType("WANGQI_DOCUMENT");
        preview.setContentId("doc-1");
        when(searchIndexGateway.getPreview(any(), any())).thenReturn(preview);

        SearchPreviewResult result = service.getPreview(
                new SearchPreviewQuery("WANGQI_DOCUMENT", "doc-1", "ANONYMOUS", null, "req-preview", "trace-preview"));

        assertEquals("doc-1", result.getContentId());
        verify(searchIndexGateway).getPreview("WANGQI_DOCUMENT", "doc-1");
    }

    @Test
    void previewShouldHideMissingOrUnauthorizedDocument() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        when(searchIndexGateway.getPreview(any(), any())).thenReturn(null);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.getPreview(new SearchPreviewQuery(
                        "SANCAI_ENTRY", "1001", "ANONYMOUS", null, "req-preview", "trace-preview")));

        assertEquals("DISCOVERY-20003", exception.getCode());
    }

    @Test
    void recordClickShouldPersistCommandPayload() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        when(searchEventRepository.getById(searchEventId("1")))
                .thenReturn(new SearchEvent(
                        1L,
                        "1",
                        "黄帝",
                        "黄帝",
                        "黄帝",
                        SearchIntentType.KEYWORD_SEARCH,
                        null,
                        1,
                        1,
                        10L,
                        "SUCCEEDED",
                        null,
                        null,
                        "ANONYMOUS",
                        null,
                        null,
                        null,
                        Instant.now()));

        Boolean result = service.recordClick(new SearchClickEventCreateCommand(
                searchEventId("1"),
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

        verify(searchClickEventRepository).save(any());
        assertTrue(result);
    }

    @Test
    void recordClickShouldRejectUnknownSearchEventId() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        when(searchEventRepository.getById(searchEventId("404"))).thenReturn(null);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.recordClick(new SearchClickEventCreateCommand(
                        searchEventId("404"),
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
    void pageEventsShouldUseFirstIntentAndStatusFilter() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        when(searchEventRepository.page("黄帝", "ENTITY", "SUCCEEDED", "user-1", 1, 20))
                .thenReturn(PageResult.of(
                        1,
                        20,
                        1,
                        List.of(new SearchEvent(
                                1L,
                                "1",
                                "黄帝",
                                "黄帝",
                                "黄帝",
                                SearchIntentType.KEYWORD_SEARCH,
                                null,
                                1,
                                1,
                                10L,
                                "SUCCEEDED",
                                null,
                                null,
                                "USER",
                                "user-1",
                                "req-1",
                                "trace-1",
                                Instant.ofEpochMilli(1_718_000_000_000L)))));

        var result = service.pageEvents(new SearchEventPageQuery(
                "黄帝", List.of("ENTITY", "KEYWORD"), List.of("SUCCEEDED", "FAILED"), "user-1", null, null, 1, 20));

        verify(searchEventRepository).page("黄帝", "ENTITY", "SUCCEEDED", "user-1", 1, 20);
        assertEquals(1, result.getRecords().size());
        assertEquals(searchEventId("1"), result.getRecords().get(0).getId());
    }

    @Test
    void getStatisticsSummaryShouldAggregateSearchEventsAndClicks() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        Instant dateFrom = Instant.ofEpochMilli(1_718_000_000_000L);
        Instant dateTo = Instant.ofEpochMilli(1_720_419_200_000L);
        when(searchEventRepository.listByCreatedAtRange(dateFrom, dateTo))
                .thenReturn(List.of(
                        searchEvent("黄帝", "SUCCEEDED", 3),
                        searchEvent("黄帝", "SUCCEEDED", 0),
                        searchEvent("天文", "FAILED", 0),
                        searchEvent("地理", "SUCCEEDED", 1),
                        searchEvent("地理", "SUCCEEDED", 2),
                        searchEvent("礼制", "SUCCEEDED", 1)));
        when(searchClickEventRepository.countByCreatedAtRange(dateFrom, dateTo)).thenReturn(7L);

        var result = service.getStatisticsSummary(new SearchStatisticsSummaryQuery(dateFrom, dateTo));

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
    void getStatisticsSummaryShouldLimitTopQueriesToTen() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                mock(SearchIndexGateway.class),
                mock(QueryUnderstandingApplicationService.class));
        when(searchEventRepository.listByCreatedAtRange(null, null))
                .thenReturn(List.of(
                        searchEvent("q01", "SUCCEEDED", 1),
                        searchEvent("q02", "SUCCEEDED", 1),
                        searchEvent("q03", "SUCCEEDED", 1),
                        searchEvent("q04", "SUCCEEDED", 1),
                        searchEvent("q05", "SUCCEEDED", 1),
                        searchEvent("q06", "SUCCEEDED", 1),
                        searchEvent("q07", "SUCCEEDED", 1),
                        searchEvent("q08", "SUCCEEDED", 1),
                        searchEvent("q09", "SUCCEEDED", 1),
                        searchEvent("q10", "SUCCEEDED", 1),
                        searchEvent("q11", "SUCCEEDED", 1)));

        var result = service.getStatisticsSummary(new SearchStatisticsSummaryQuery(null, null));

        assertEquals(10, result.getTopQueries().size());
        assertEquals("q01", result.getTopQueries().get(0).getQueryText());
        assertEquals("q10", result.getTopQueries().get(9).getQueryText());
    }

    @Test
    void searchShouldPersistFailureLogAndRethrowBizException() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
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
                requestId("req-2"),
                traceId("trace-2"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenReturn(new QueryUnderstandingResult("黄帝", "黄帝", "KEYWORD_SEARCH", List.of(), "req-2", "trace-2"));
        when(searchIndexGateway.search(any(), any(), any(Integer.class), any(Integer.class)))
                .thenThrow(new BizException("DISCOVERY-29999", "discovery.search.test", "boom"));

        BizException exception = assertThrows(BizException.class, () -> service.search(query));
        ArgumentCaptor<SearchEvent> searchEventCaptor = ArgumentCaptor.forClass(SearchEvent.class);

        assertEquals("DISCOVERY-29999", exception.getCode());
        verify(searchEventRepository, times(1)).save(searchEventCaptor.capture());
        assertEquals("FAILED", searchEventCaptor.getValue().getSearchStatus());
        assertEquals("DISCOVERY-29999", searchEventCaptor.getValue().getFailureCode());
        assertEquals("boom", searchEventCaptor.getValue().getFailureMessage());
        assertTrue(searchEventCaptor.getValue().getSearchLatencyMs() >= 0);
    }

    @Test
    void searchShouldPersistFailureLogWhenQueryUnderstandingFails() {
        SearchEventRepository searchEventRepository = mock(SearchEventRepository.class);
        SearchClickEventRepository searchClickEventRepository = mock(SearchClickEventRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchEventRepository,
                searchClickEventRepository,
                new SearchDomainService(),
                searchIndexGateway,
                queryUnderstandingApplicationService);
        SearchQuery query = new SearchQuery(
                "辞官",
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
                requestId("req-query-understanding"),
                traceId("trace-query-understanding"));
        when(queryUnderstandingApplicationService.understand(query))
                .thenThrow(new BizException(
                        "DISCOVERY-20004",
                        "discovery.search.query-understanding.result-parse-failed",
                        "Query understanding result parse failed"));

        BizException exception = assertThrows(BizException.class, () -> service.search(query));
        ArgumentCaptor<SearchEvent> searchEventCaptor = ArgumentCaptor.forClass(SearchEvent.class);

        assertEquals("DISCOVERY-20004", exception.getCode());
        verify(searchEventRepository).save(searchEventCaptor.capture());
        verify(searchIndexGateway, times(0)).search(any(), any(), any(Integer.class), any(Integer.class));
        assertEquals("FAILED", searchEventCaptor.getValue().getSearchStatus());
        assertEquals("DISCOVERY-20004", searchEventCaptor.getValue().getFailureCode());
        assertEquals("辞官", searchEventCaptor.getValue().getQueryText());
        assertEquals("辞官", searchEventCaptor.getValue().getNormalizedQueryText());
        assertEquals(
                "KEYWORD_SEARCH", searchEventCaptor.getValue().getIntentType().value());
        assertTrue(searchEventCaptor.getValue().getSearchLatencyMs() >= 0);
    }

    private SearchPageResult searchPageResult(int totalCount, SearchGroupResult... groups) {
        return new SearchPageResult(totalCount, List.of(groups));
    }

    private SearchEvent searchEvent(String queryText, String searchStatus, Integer resultTotalCount) {
        return new SearchEvent(
                1L,
                "1",
                queryText,
                queryText,
                queryText,
                SearchIntentType.KEYWORD_SEARCH,
                null,
                resultTotalCount,
                resultTotalCount == null ? 0 : Math.min(resultTotalCount, 1),
                10L,
                searchStatus,
                null,
                null,
                "ANONYMOUS",
                null,
                null,
                null,
                Instant.now());
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

    private SearchEventId searchEventId(String value) {
        return SearchEventIdCodec.toDomain(value);
    }

    private com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId requestId(String value) {
        return RequestIdCodec.toDomain(value);
    }

    private com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId traceId(String value) {
        return TraceIdCodec.toDomain(value);
    }
}
