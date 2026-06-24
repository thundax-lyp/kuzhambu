package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultSearchPermissionFilter implements SearchPermissionFilter {

    @Override
    public List<SearchGroupResult> filter(SearchQuery query, List<SearchGroupResult> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return groups;
    }
}
