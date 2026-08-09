package com.thundax.kuzhambu.classics.application.mingcustoms.query;

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
public class MingCustomsQuery {
    private String category;
    private String keyword;
    private String tagName;
    private Long tagId;
    private String tagNameSnapshot;
    private SortDirection sortDirection = SortDirection.ASC;
    private Set<String> operatorPermissions;

    public MingCustomsQuery(String category, String keyword, String tagName, SortDirection sortDirection) {
        this(category, keyword, tagName, null, null, sortDirection, null);
    }
}
