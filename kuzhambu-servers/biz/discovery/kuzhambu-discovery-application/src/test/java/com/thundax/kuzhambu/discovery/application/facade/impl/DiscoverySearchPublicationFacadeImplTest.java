package com.thundax.kuzhambu.discovery.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.facade.assembler.DiscoverySearchPublicationFacadeAssembler;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationPrepareCommand;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationReferenceCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCandidateQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCategoryAggregationQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCategoryAggregationResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationProbeResult;
import com.thundax.kuzhambu.discovery.application.search.service.SearchPublicationApplicationService;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationCandidatePageFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationCategoryAggregationFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DiscoverySearchPublicationFacadeImplTest {

    private final SearchPublicationApplicationService service = mock(SearchPublicationApplicationService.class);
    private final DiscoverySearchPublicationFacadeImpl facade =
            new DiscoverySearchPublicationFacadeImpl(service, new DiscoverySearchPublicationFacadeAssembler());

    @Test
    void prepareShouldPreservePublicationIdentityAndOrderedPayload() {
        DiscoverySearchPublicationPrepareFacadeRequest request =
                DiscoverySearchPublicationPrepareFacadeRequest.builder()
                        .sourceId("SANCAI_ENTRY:101")
                        .contentType("SANCAI_ENTRY")
                        .contentId("101")
                        .contentVersionId("9001")
                        .contentVersionNo(7)
                        .title("天文")
                        .textSegments(List.of("标题", "正文"))
                        .tagNames(List.of("星象"))
                        .build();
        ArgumentCaptor<SearchPublicationPrepareCommand> captor =
                ArgumentCaptor.forClass(SearchPublicationPrepareCommand.class);

        facade.prepare(request);

        verify(service).prepare(captor.capture());
        assertEquals("SANCAI_ENTRY:101", captor.getValue().getSourceId());
        assertEquals("9001", captor.getValue().getContentVersionId());
        assertEquals(List.of("标题", "正文"), captor.getValue().getTextSegments());
    }

    @Test
    void operationsShouldUseSingleReferenceContractAndMapProbe() {
        DiscoverySearchPublicationReferenceFacadeRequest request =
                DiscoverySearchPublicationReferenceFacadeRequest.builder()
                        .documentId("SANCAI_ENTRY:101")
                        .contentVersionId("9001")
                        .contentVersionNo(7)
                        .build();
        when(service.probe(any(SearchPublicationReferenceCommand.class)))
                .thenReturn(new SearchPublicationProbeResult(true, "READY", false, "9001", 7));

        facade.markReady(request);
        facade.markOffline(request);
        facade.delete(request);
        var result = facade.probe(request);

        verify(service).markReady(any(SearchPublicationReferenceCommand.class));
        verify(service).markOffline(any(SearchPublicationReferenceCommand.class));
        verify(service).delete(any(SearchPublicationReferenceCommand.class));
        assertTrue(result.isPresent());
        assertEquals("READY", result.getPublicationStatus());
    }

    @Test
    void pageReadyCandidatesShouldDefaultNullableFacadePagination() {
        when(service.pageReadyCandidates(any(SearchPublicationCandidateQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 10, 0, List.of()));
        ArgumentCaptor<SearchPublicationCandidateQuery> queryCaptor =
                ArgumentCaptor.forClass(SearchPublicationCandidateQuery.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);

        facade.pageReadyCandidates(DiscoverySearchPublicationCandidatePageFacadeRequest.builder()
                .contentType("SANCAI_ENTRY")
                .build());

        verify(service).pageReadyCandidates(queryCaptor.capture(), pageCaptor.capture());
        assertEquals("SANCAI_ENTRY", queryCaptor.getValue().contentType());
        assertEquals(1, pageCaptor.getValue().getPageNo());
        assertEquals(10, pageCaptor.getValue().getPageSize());
    }

    @Test
    void listReadyCandidateCategoryAggregationsShouldMapRequestAndResponse() {
        when(service.listReadyCandidateCategoryAggregations(any(SearchPublicationCategoryAggregationQuery.class)))
                .thenReturn(List.of(new SearchPublicationCategoryAggregationResult("11", 13, "1001")));
        ArgumentCaptor<SearchPublicationCategoryAggregationQuery> captor =
                ArgumentCaptor.forClass(SearchPublicationCategoryAggregationQuery.class);

        var result = facade.listReadyCandidateCategoryAggregations(
                DiscoverySearchPublicationCategoryAggregationFacadeRequest.builder()
                        .contentType("SANCAI_ENTRY")
                        .build());

        verify(service).listReadyCandidateCategoryAggregations(captor.capture());
        assertEquals("SANCAI_ENTRY", captor.getValue().getContentType());
        assertEquals("11", result.get(0).getCategoryId());
        assertEquals(13, result.get(0).getReadyEntryCount());
        assertEquals("1001", result.get(0).getRepresentativeContentId());
    }
}
