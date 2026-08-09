package com.thundax.kuzhambu.discovery.application.search.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickEventCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchEventQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchPreviewQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchStatisticsSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchEventResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPreviewResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchStatisticsSummaryResult;

public interface SearchApplicationService {

    SearchEventResult search(SearchQuery query);

    SearchPreviewResult getPreview(SearchPreviewQuery query);

    Boolean recordClick(SearchClickEventCreateCommand command);

    PageResult<SearchEventResult> pageEvents(SearchEventQuery query, PageQuery pageQuery);

    SearchEventResult getEvent(Long id);

    SearchStatisticsSummaryResult getStatisticsSummary(SearchStatisticsSummaryQuery query);
}
