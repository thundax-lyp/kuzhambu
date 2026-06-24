package com.thundax.kuzhambu.discovery.application.search.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class QueryUnderstandingApplicationServiceImpl implements QueryUnderstandingApplicationService {

    @Override
    public String understand(SearchQuery query) {
        if (query == null
                || query.getQueryText() == null
                || query.getQueryText().isBlank()) {
            throw new BizException("Search query is required");
        }
        throw new BizException(
                "DISCOVERY-20002",
                "discovery.search.query-understanding.not-implemented",
                "Query understanding is not implemented");
    }
}
