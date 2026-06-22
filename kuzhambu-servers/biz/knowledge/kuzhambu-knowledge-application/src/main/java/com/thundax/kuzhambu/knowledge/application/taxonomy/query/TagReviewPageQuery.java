package com.thundax.kuzhambu.knowledge.application.taxonomy.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagReviewPageQuery {
    private String name;
    private TagSource source;
    private SortDirection sortDirection = SortDirection.ASC;
}
