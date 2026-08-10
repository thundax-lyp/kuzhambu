package com.thundax.kuzhambu.classics.application.wangqi.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Set;

public record WangqiDocumentQuery(String keyword, SortDirection sortDirection, Set<String> operatorPermissions) {
    public WangqiDocumentQuery(String keyword, SortDirection sortDirection) {
        this(keyword, sortDirection, null);
    }
}
