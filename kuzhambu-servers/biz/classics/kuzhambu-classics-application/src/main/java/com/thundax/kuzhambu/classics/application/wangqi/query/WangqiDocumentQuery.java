package com.thundax.kuzhambu.classics.application.wangqi.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WangqiDocumentQuery {
    private String keyword;
    private SortDirection sortDirection = SortDirection.ASC;
    private Set<String> operatorPermissions;

    public WangqiDocumentQuery(String keyword, SortDirection sortDirection) {
        this(keyword, sortDirection, null);
    }
}
