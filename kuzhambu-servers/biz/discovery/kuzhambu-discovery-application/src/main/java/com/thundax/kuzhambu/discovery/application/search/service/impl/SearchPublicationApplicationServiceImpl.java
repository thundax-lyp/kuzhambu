package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationPrepareCommand;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationReferenceCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCandidateQuery;
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

    private static final int MAX_PAGE_SIZE = 500;

    private final SearchIndexGateway searchIndexGateway;

    public SearchPublicationApplicationServiceImpl(SearchIndexGateway searchIndexGateway) {
        this.searchIndexGateway = searchIndexGateway;
    }

    @Override
    public void prepare(SearchPublicationPrepareCommand command) {
        if (command == null || command.sourceId() == null || command.sourceId().isBlank()) {
            throw new BizException("DISCOVERY_PUBLICATION_DOCUMENT_INVALID");
        }
        searchIndexGateway.preparePublication(new SearchPublicationDocument(
                command.sourceId(),
                command.contentType(),
                command.contentId(),
                command.contentVersionId(),
                command.contentVersionNo(),
                command.title(),
                command.summary(),
                command.categoryId(),
                command.categoryName(),
                command.volumeId(),
                command.volumeTitle(),
                command.textSegments() == null ? Collections.emptyList() : command.textSegments(),
                command.tagNames() == null ? Collections.emptyList() : command.tagNames(),
                command.contentUpdatedAt()));
    }

    @Override
    public void markReady(SearchPublicationReferenceCommand command) {
        if (!searchIndexGateway.markPublicationReady(
                command.documentId(), command.contentVersionId(), command.contentVersionNo())) {
            throw new BizException("DISCOVERY_PUBLICATION_VERSION_MISMATCH");
        }
    }

    @Override
    public void markOffline(SearchPublicationReferenceCommand command) {
        searchIndexGateway.markPublicationOffline(command.documentId(), command.occurredAt());
    }

    @Override
    public void delete(SearchPublicationReferenceCommand command) {
        searchIndexGateway.deletePublication(command.documentId());
    }

    @Override
    public SearchPublicationProbeResult probe(SearchPublicationReferenceCommand command) {
        return searchIndexGateway.probePublication(command.documentId());
    }

    @Override
    public PageResult<SearchPublicationCandidateResult> pageReadyCandidates(
            SearchPublicationCandidateQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        return searchIndexGateway.pageReadyPublicationCandidates(
                query == null ? null : query.contentType(),
                query == null ? null : query.categoryId(),
                query == null ? null : query.volumeId(),
                query == null ? null : query.keyword(),
                effectivePage.getPageNo(),
                Math.min(effectivePage.getPageSize(), MAX_PAGE_SIZE));
    }

    @Override
    public List<SearchPublicationCategoryAggregationResult> listReadyCandidateCategoryAggregations(
            SearchPublicationCategoryAggregationQuery query) {
        return searchIndexGateway.listReadyPublicationCategoryAggregations(query == null ? null : query.contentType());
    }
}
