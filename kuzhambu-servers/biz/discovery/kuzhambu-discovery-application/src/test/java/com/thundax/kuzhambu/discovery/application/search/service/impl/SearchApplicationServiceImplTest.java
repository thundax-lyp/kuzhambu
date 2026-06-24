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
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchLogPageQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import com.thundax.kuzhambu.discovery.application.search.support.DefaultSearchPermissionFilter;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchLog;
import com.thundax.kuzhambu.discovery.domain.search.model.enums.SearchIntentType;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickRepository;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchLogRepository;
import com.thundax.kuzhambu.discovery.domain.service.SearchDomainService;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SearchApplicationServiceImplTest {

    @Test
    void searchShouldTranslateBackendNotImplementedToBizException() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter());
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
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter());
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
                                null,
                                1,
                                1,
                                "/classics/sancai/1001")))));

        var result = service.search(new SearchQuery(
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
                "trace-1"));
        ArgumentCaptor<SearchLog> searchLogCaptor = ArgumentCaptor.forClass(SearchLog.class);

        verify(searchLogRepository).save(searchLogCaptor.capture());
        assertEquals(1, result.getGroups().size());
        assertEquals(1, result.getResultTotalCount());
        assertEquals("黄帝", result.getDisplayQueryText());
        assertEquals("SUCCEEDED", searchLogCaptor.getValue().getSearchStatus());
        assertEquals(searchLogCaptor.getValue().getSearchLogId(), result.getSearchLogId());
        assertTrue(result.getSearchScopesJson().contains("SANCAI_ENTRY"));
    }

    @Test
    void recordClickShouldPersistCommandPayload() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter());
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
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter());
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
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter());
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
    void searchShouldPersistFailureLogAndRethrowBizException() {
        SearchLogRepository searchLogRepository = mock(SearchLogRepository.class);
        SearchClickRepository searchClickRepository = mock(SearchClickRepository.class);
        SearchIndexGateway searchIndexGateway = mock(SearchIndexGateway.class);
        SearchApplicationServiceImpl service = new SearchApplicationServiceImpl(
                searchLogRepository,
                searchClickRepository,
                new SearchDomainService(),
                searchIndexGateway,
                new DefaultSearchPermissionFilter());
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
}
