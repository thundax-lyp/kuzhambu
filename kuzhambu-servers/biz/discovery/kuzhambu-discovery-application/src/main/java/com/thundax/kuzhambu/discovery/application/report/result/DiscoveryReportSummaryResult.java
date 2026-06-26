package com.thundax.kuzhambu.discovery.application.report.result;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryReportSummaryResult {

    private Date periodStart;
    private Date periodEnd;
    private Long searchCount;
    private Long qaCount;
    private Long avgSearchLatencyMs;
    private List<TopQueryResult> topQueries;
    private List<SearchTrendPointResult> searchTrendSeries;
    private List<QaTrendPointResult> qaTrendSeries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopQueryResult {

        private String queryText;
        private Long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchTrendPointResult {

        private String bucket;
        private Long searchCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QaTrendPointResult {

        private String bucket;
        private Long qaCount;
    }
}
