package com.thundax.kuzhambu.discovery.application.facade.assembler;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.page.PageRules;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationPrepareCommand;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationReferenceCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCandidateQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCategoryAggregationQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCandidateResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCategoryAggregationResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationProbeResult;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationCandidatePageFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationCategoryAggregationFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationCandidateFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationCandidatePageFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationCategoryAggregationFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationProbeFacadeResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiscoverySearchPublicationFacadeAssembler {

    public SearchPublicationPrepareCommand toPrepareCommand(DiscoverySearchPublicationPrepareFacadeRequest request) {
        return new SearchPublicationPrepareCommand(
                request.getSourceId(),
                request.getContentType(),
                request.getContentId(),
                request.getContentVersionId(),
                request.getContentVersionNo(),
                request.getTitle(),
                request.getSummary(),
                request.getCategoryId(),
                request.getCategoryName(),
                request.getVolumeId(),
                request.getVolumeTitle(),
                request.getTextSegments(),
                request.getTagNames(),
                request.getContentUpdatedAt());
    }

    public SearchPublicationReferenceCommand toReferenceCommand(
            DiscoverySearchPublicationReferenceFacadeRequest request) {
        return new SearchPublicationReferenceCommand(
                request.getDocumentId(),
                request.getContentVersionId(),
                request.getContentVersionNo(),
                request.getOccurredAt());
    }

    public SearchPublicationCandidateQuery toCandidateQuery(
            DiscoverySearchPublicationCandidatePageFacadeRequest request) {
        return new SearchPublicationCandidateQuery(
                request == null ? null : request.getContentType(),
                request == null ? null : request.getCategoryId(),
                request == null ? null : request.getVolumeId(),
                request == null ? null : request.getKeyword());
    }

    public PageQuery toPageQuery(DiscoverySearchPublicationCandidatePageFacadeRequest request) {
        return new PageQuery(pageNo(request), pageSize(request));
    }

    public SearchPublicationCategoryAggregationQuery toCategoryAggregationQuery(
            DiscoverySearchPublicationCategoryAggregationFacadeRequest request) {
        return new SearchPublicationCategoryAggregationQuery(request == null ? null : request.getContentType());
    }

    public DiscoverySearchPublicationProbeFacadeResponse toProbeResponse(SearchPublicationProbeResult result) {
        return DiscoverySearchPublicationProbeFacadeResponse.builder()
                .present(result.isPresent())
                .publicationStatus(result.getPublicationStatus())
                .deleted(result.getDeleted())
                .contentVersionId(result.getContentVersionId())
                .contentVersionNo(result.getContentVersionNo())
                .build();
    }

    public DiscoverySearchPublicationCandidatePageFacadeResponse toCandidatePageResponse(
            PageResult<SearchPublicationCandidateResult> result) {
        List<DiscoverySearchPublicationCandidateFacadeResponse> records = result == null || result.getRecords() == null
                ? List.of()
                : result.getRecords().stream().map(this::toCandidateResponse).toList();
        return DiscoverySearchPublicationCandidatePageFacadeResponse.builder()
                .pageNo(result == null ? 1 : result.getPageNo())
                .pageSize(result == null ? 0 : result.getPageSize())
                .totalCount(result == null ? 0 : result.getTotalCount())
                .records(records)
                .build();
    }

    private DiscoverySearchPublicationCandidateFacadeResponse toCandidateResponse(
            SearchPublicationCandidateResult result) {
        return DiscoverySearchPublicationCandidateFacadeResponse.builder()
                .contentType(result.getContentType())
                .contentId(result.getContentId())
                .categoryId(result.getCategoryId())
                .volumeId(result.getVolumeId())
                .build();
    }

    private int pageNo(DiscoverySearchPublicationCandidatePageFacadeRequest request) {
        Integer pageNo = request == null ? null : request.getPageNo();
        return pageNo == null ? PageRules.firstPageIndex() : pageNo;
    }

    private int pageSize(DiscoverySearchPublicationCandidatePageFacadeRequest request) {
        Integer pageSize = request == null ? null : request.getPageSize();
        return pageSize == null ? PageRules.defaultPageSize() : pageSize;
    }

    public List<DiscoverySearchPublicationCategoryAggregationFacadeResponse> toCategoryAggregationResponses(
            List<SearchPublicationCategoryAggregationResult> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream().map(this::toCategoryAggregationResponse).toList();
    }

    private DiscoverySearchPublicationCategoryAggregationFacadeResponse toCategoryAggregationResponse(
            SearchPublicationCategoryAggregationResult result) {
        return DiscoverySearchPublicationCategoryAggregationFacadeResponse.builder()
                .categoryId(result.getCategoryId())
                .readyEntryCount(result.getReadyEntryCount())
                .representativeContentId(result.getRepresentativeContentId())
                .build();
    }
}
