package com.thundax.kuzhambu.knowledge.application.taxonomy.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;

public record TagQuery(
        String name,
        TagCategoryId categoryId,
        TagStatus status,
        TagSource source,
        TagReviewStatus reviewStatus,
        SortDirection sortDirection) {

    public TagQuery {
        sortDirection = sortDirection == null ? SortDirection.ASC : sortDirection;
    }
}
