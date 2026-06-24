package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import java.util.List;

public interface SearchPermissionFilter {

    List<SearchGroupResult> filter(SearchQuery query, List<SearchGroupResult> groups);
}
