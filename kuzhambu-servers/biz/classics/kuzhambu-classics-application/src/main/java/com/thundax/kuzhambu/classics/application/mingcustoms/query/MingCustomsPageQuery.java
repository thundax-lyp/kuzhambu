package com.thundax.kuzhambu.classics.application.mingcustoms.query;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
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
public class MingCustomsPageQuery {
    private String category;
    private String keyword;
    private String tagName;
    private Long tagId;
    private String tagNameSnapshot;
    private MingCustomsVisibility visibility;
    private SortDirection sortDirection = SortDirection.ASC;
    private Set<String> operatorPermissions;

    public MingCustomsPageQuery(
            String category,
            String keyword,
            String tagName,
            MingCustomsVisibility visibility,
            SortDirection sortDirection) {
        this(category, keyword, tagName, null, null, visibility, sortDirection, null);
    }
}
