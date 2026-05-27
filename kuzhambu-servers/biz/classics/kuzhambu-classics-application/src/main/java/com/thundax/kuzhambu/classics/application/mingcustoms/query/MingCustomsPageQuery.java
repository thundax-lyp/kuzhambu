package com.thundax.kuzhambu.classics.application.mingcustoms.query;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MingCustomsPageQuery {
    private String category;
    private String keyword;
    private String tagName;
    private MingCustomsVisibility visibility;
    private SortDirection sortDirection = SortDirection.ASC;
}
