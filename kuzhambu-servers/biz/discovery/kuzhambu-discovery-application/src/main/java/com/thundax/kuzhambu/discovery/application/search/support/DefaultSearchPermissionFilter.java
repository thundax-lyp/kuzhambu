package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.permission.PermissionMatcher;
import com.thundax.kuzhambu.common.security.permission.PrefixPermissionMatcher;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.SearchGroupResult;
import com.thundax.kuzhambu.discovery.application.search.result.SearchResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DefaultSearchPermissionFilter implements SearchPermissionFilter {

    private static final String PUBLIC_VISIBILITY = "PUBLIC";
    private static final String SUPER_PERMISSION = "super";
    private static final String CLASSICS_CONTENT_VIEW_PERMISSION = "classics:content:view";
    private static final String SANCAI_ENTRY_TYPE = "SANCAI_ENTRY";
    private static final String WANGQI_DOCUMENT_TYPE = "WANGQI_DOCUMENT";
    private static final String MING_CUSTOMS_TYPE = "MING_CUSTOMS";
    private static final PermissionMatcher PERMISSION_MATCHER = new PrefixPermissionMatcher();

    @Override
    public List<SearchGroupResult> filter(SearchQuery query, List<SearchGroupResult> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> authorities = KuzhambuContextHolder.currentAuthorities();
        List<SearchGroupResult> filteredGroups = new ArrayList<>();
        for (SearchGroupResult group : groups) {
            if (group == null || group.getItems() == null || group.getItems().isEmpty()) {
                continue;
            }
            List<SearchResult> visibleItems = group.getItems().stream()
                    .filter(item -> isVisible(item, authorities))
                    .toList();
            if (!visibleItems.isEmpty()) {
                filteredGroups.add(new SearchGroupResult(
                        group.getGroupKey(), group.getGroupTitle(), visibleItems.size(), visibleItems));
            }
        }
        return filteredGroups;
    }

    private boolean isVisible(SearchResult item, Set<String> authorities) {
        if (item == null) {
            return false;
        }
        if (isBlank(item.getVisibility()) || PUBLIC_VISIBILITY.equalsIgnoreCase(item.getVisibility())) {
            return true;
        }
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        String contentPermission = contentViewPermission(item.getContentType());
        if (contentPermission == null) {
            return false;
        }
        if (authorities.contains(SUPER_PERMISSION)
                || PERMISSION_MATCHER.matches(authorities, CLASSICS_CONTENT_VIEW_PERMISSION)) {
            return true;
        }
        return PERMISSION_MATCHER.matches(authorities, contentPermission);
    }

    private String contentViewPermission(String contentType) {
        if (SANCAI_ENTRY_TYPE.equals(contentType)) {
            return "classics:sancai:view";
        }
        if (WANGQI_DOCUMENT_TYPE.equals(contentType)) {
            return "classics:wangqi:view";
        }
        if (MING_CUSTOMS_TYPE.equals(contentType)) {
            return "classics:mingcustoms:view";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
