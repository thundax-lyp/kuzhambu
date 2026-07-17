package com.thundax.kuzhambu.discovery.application.search.result;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPageResult {
    private int totalCount;
    private List<SearchGroupResult> groups;

    public List<SearchGroupResult> safeGroups() {
        return groups == null ? Collections.emptyList() : groups;
    }
}
