package com.thundax.kuzhambu.classics.application.wangqi.query;

import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WangqiDocumentPageQuery {
    private String keyword;
    private WangqiDocumentVisibility visibility;
    private SortDirection sortDirection = SortDirection.ASC;
}
