package com.thundax.kuzhambu.knowledge.application.taxonomy.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;

public record TagReviewQuery(String name, TagSource source, SortDirection sortDirection) {

    public TagReviewQuery {
        sortDirection = sortDirection == null ? SortDirection.ASC : sortDirection;
    }
}
