package com.thundax.kuzhambu.discovery.application.search.service;

import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;

public interface QueryUnderstandingApplicationService {

    QueryUnderstandingResult understand(SearchQuery query);
}
