package com.thundax.kuzhambu.discovery.application.search.service;

import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;

public interface QueryUnderstandingApplicationService {

    String understand(SearchQuery query);
}
