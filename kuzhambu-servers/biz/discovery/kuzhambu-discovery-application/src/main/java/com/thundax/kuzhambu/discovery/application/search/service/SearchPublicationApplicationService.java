package com.thundax.kuzhambu.discovery.application.search.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationPrepareCommand;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationReferenceCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCandidateQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPublicationCategoryAggregationQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCandidateResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationCategoryAggregationResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationProbeResult;
import java.util.List;

public interface SearchPublicationApplicationService {

    void prepare(SearchPublicationPrepareCommand command);

    void markReady(SearchPublicationReferenceCommand command);

    void markOffline(SearchPublicationReferenceCommand command);

    void delete(SearchPublicationReferenceCommand command);

    SearchPublicationProbeResult probe(SearchPublicationReferenceCommand command);

    PageResult<SearchPublicationCandidateResult> pageReadyCandidates(
            SearchPublicationCandidateQuery query, PageQuery pageQuery);

    List<SearchPublicationCategoryAggregationResult> listReadyCandidateCategoryAggregations(
            SearchPublicationCategoryAggregationQuery query);
}
