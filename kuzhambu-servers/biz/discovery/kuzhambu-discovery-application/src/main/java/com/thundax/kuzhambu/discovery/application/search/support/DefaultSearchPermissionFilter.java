package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import java.util.ArrayList;
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
        List<SearchGroupResult> filteredGroups = new ArrayList<>();
        for (SearchGroupResult group : groups) {
            if (group == null || group.getItems() == null || group.getItems().isEmpty()) {
                continue;
            }
            List<SearchResult> visibleItems =
                    group.getItems().stream().filter(item -> item != null).toList();
            if (!visibleItems.isEmpty()) {
                filteredGroups.add(new SearchGroupResult(
                        group.getGroupKey(), group.getGroupTitle(), visibleItems.size(), visibleItems));
            }
        }
        return filteredGroups;
    }
}
