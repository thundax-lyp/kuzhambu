package com.thundax.kuzhambu.discovery.application.search.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchGroupResult {
    private String groupKey;
    private String groupTitle;
    private int count;
    private List<SearchResult> items;
}
