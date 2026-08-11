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
import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DiscoverySearchPublicationFacadeAssembler {

    @NonNull
    public SearchPublicationPrepareCommand toPrepareCommand(
            @NonNull DiscoverySearchPublicationPrepareFacadeRequest request) {
        Objects.requireNonNull(request, "request");
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

    @NonNull
    public SearchPublicationReferenceCommand toReferenceCommand(
            @NonNull DiscoverySearchPublicationReferenceFacadeRequest request) {
        Objects.requireNonNull(request, "request");
        return new SearchPublicationReferenceCommand(
                request.getDocumentId(),
                request.getContentVersionId(),
                request.getContentVersionNo(),
                request.getOccurredAt());
    }

    @NonNull
    public SearchPublicationCandidateQuery toCandidateQuery(
            @NonNull DiscoverySearchPublicationCandidatePageFacadeRequest request) {
        Objects.requireNonNull(request, "request");
        return new SearchPublicationCandidateQuery(
                request.getContentType(), request.getCategoryId(), request.getVolumeId(), request.getKeyword());
    }

    @NonNull
    public PageQuery toPageQuery(@NonNull DiscoverySearchPublicationCandidatePageFacadeRequest request) {
        Objects.requireNonNull(request, "request");
        return new PageQuery(pageNo(request), pageSize(request));
    }

    @NonNull
    public SearchPublicationCategoryAggregationQuery toCategoryAggregationQuery(
            @NonNull DiscoverySearchPublicationCategoryAggregationFacadeRequest request) {
        Objects.requireNonNull(request, "request");
        return new SearchPublicationCategoryAggregationQuery(request.getContentType());
    }

    @NonNull
    public DiscoverySearchPublicationProbeFacadeResponse toProbeResponse(@NonNull SearchPublicationProbeResult result) {
        Objects.requireNonNull(result, "result");
        return DiscoverySearchPublicationProbeFacadeResponse.builder()
                .present(result.isPresent())
                .publicationStatus(result.getPublicationStatus())
                .deleted(result.getDeleted())
                .contentVersionId(result.getContentVersionId())
                .contentVersionNo(result.getContentVersionNo())
                .build();
    }

    @NonNull
    public DiscoverySearchPublicationCandidatePageFacadeResponse toCandidatePageResponse(
            @NonNull PageResult<SearchPublicationCandidateResult> result) {
        Objects.requireNonNull(result, "result");
        List<DiscoverySearchPublicationCandidateFacadeResponse> records = result.getRecords() == null
                ? List.of()
                : result.getRecords().stream().map(this::toCandidateResponse).toList();
        return DiscoverySearchPublicationCandidatePageFacadeResponse.builder()
                .pageNo(result.getPageNo())
                .pageSize(result.getPageSize())
                .totalCount(result.getTotalCount())
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
        Integer pageNo = request.getPageNo();
        return pageNo == null ? PageRules.firstPageIndex() : pageNo;
    }

    private int pageSize(DiscoverySearchPublicationCandidatePageFacadeRequest request) {
        Integer pageSize = request.getPageSize();
        return pageSize == null ? PageRules.defaultPageSize() : pageSize;
    }

    @NonNull
    public List<DiscoverySearchPublicationCategoryAggregationFacadeResponse> toCategoryAggregationResponses(
            @NonNull List<SearchPublicationCategoryAggregationResult> results) {
        Objects.requireNonNull(results, "results");
        if (results.isEmpty()) {
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
