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
public class SearchStatisticsSummaryResult {
    private long searchCount;
    private long failedSearchCount;
    private long zeroResultSearchCount;
    private long clickCount;
    private List<TopQueryItem> topQueries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopQueryItem {
        private String queryText;
        private long count;
    }
}
