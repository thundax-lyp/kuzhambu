package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationPrepareCommand;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationReferenceCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCandidatePageQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCategoryAggregationQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCandidateResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCategoryAggregationResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationDocument;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationProbeResult;
import com.thundax.kuzhambu.discovery.application.search.service.SearchPublicationApplicationService;
import com.thundax.kuzhambu.discovery.application.search.support.SearchIndexGateway;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class SearchPublicationApplicationServiceImpl implements SearchPublicationApplicationService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 500;

    private final SearchIndexGateway searchIndexGateway;

    public SearchPublicationApplicationServiceImpl(SearchIndexGateway searchIndexGateway) {
        this.searchIndexGateway = searchIndexGateway;
    }

    @Override
    public void prepare(SearchPublicationPrepareCommand command) {
        if (command == null
                || command.getSourceId() == null
                || command.getSourceId().isBlank()) {
            throw new BizException("DISCOVERY_PUBLICATION_DOCUMENT_INVALID");
        }
        searchIndexGateway.preparePublication(new SearchPublicationDocument(
                command.getSourceId(),
                command.getContentType(),
                command.getContentId(),
                command.getContentVersionId(),
                command.getContentVersionNo(),
                command.getTitle(),
                command.getSummary(),
                command.getCategoryId(),
                command.getCategoryName(),
                command.getVolumeId(),
                command.getVolumeTitle(),
                command.getTextSegments() == null ? Collections.emptyList() : command.getTextSegments(),
                command.getTagNames() == null ? Collections.emptyList() : command.getTagNames(),
                command.getContentUpdatedAt()));
    }

    @Override
    public void markReady(SearchPublicationReferenceCommand command) {
        if (!searchIndexGateway.markPublicationReady(
                command.getDocumentId(), command.getContentVersionId(), command.getContentVersionNo())) {
            throw new BizException("DISCOVERY_PUBLICATION_VERSION_MISMATCH");
        }
    }

    @Override
    public void markOffline(SearchPublicationReferenceCommand command) {
        searchIndexGateway.markPublicationOffline(command.getDocumentId(), command.getOccurredAt());
    }

    @Override
    public void delete(SearchPublicationReferenceCommand command) {
        searchIndexGateway.deletePublication(command.getDocumentId());
    }

    @Override
    public SearchPublicationProbeResult probe(SearchPublicationReferenceCommand command) {
        return searchIndexGateway.probePublication(command.getDocumentId());
    }

    @Override
    public PageResult<SearchPublicationCandidateResult> pageReadyCandidates(SearchPublicationCandidatePageQuery query) {
        return searchIndexGateway.pageReadyPublicationCandidates(
                query == null ? null : query.getContentType(),
                query == null ? null : query.getCategoryId(),
                query == null ? null : query.getVolumeId(),
                query == null ? null : query.getKeyword(),
                normalizePageNo(query == null ? null : query.getPageNo()),
                normalizePageSize(query == null ? null : query.getPageSize()));
    }

    @Override
    public List<SearchPublicationCategoryAggregationResult> listReadyCandidateCategoryAggregations(
            SearchPublicationCategoryAggregationQuery query) {
        return searchIndexGateway.listReadyPublicationCategoryAggregations(
                query == null ? null : query.getContentType());
    }

    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
