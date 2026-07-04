package com.thundax.kuzhambu.discovery.application.search.service;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.application.search.command.SearchClickCreateCommand;
import com.thundax.kuzhambu.discovery.application.search.query.SearchAnalysisSummaryQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchLogPageQuery;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchAnalysisSummaryResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchLogResult;

public interface SearchApplicationService {

    SearchLogResult search(SearchQuery query);

    Boolean recordClick(SearchClickCreateCommand command);

    PageResult<SearchLogResult> pageLogs(SearchLogPageQuery query);

    SearchLogResult getLog(String searchLogId);

    SearchAnalysisSummaryResult getAnalysisSummary(SearchAnalysisSummaryQuery query);
}
