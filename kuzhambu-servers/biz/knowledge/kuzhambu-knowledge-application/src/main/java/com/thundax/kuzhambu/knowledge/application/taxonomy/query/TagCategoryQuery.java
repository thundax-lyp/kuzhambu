package com.thundax.kuzhambu.knowledge.application.taxonomy.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;

public record TagCategoryQuery(String name, TagCategoryStatus status, SortDirection sortDirection) {

    public TagCategoryQuery {
        sortDirection = sortDirection == null ? SortDirection.ASC : sortDirection;
    }
}
