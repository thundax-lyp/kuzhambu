package com.thundax.kuzhambu.classics.application.mingcustoms.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Set;

public record MingCustomsQuery(
        String category,
        String keyword,
        String tagName,
        Long tagId,
        String tagNameSnapshot,
        SortDirection sortDirection,
        Set<String> operatorPermissions) {
    public MingCustomsQuery(String category, String keyword, String tagName, SortDirection sortDirection) {
        this(category, keyword, tagName, null, null, sortDirection, null);
    }
}
