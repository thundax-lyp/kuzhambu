package com.thundax.kuzhambu.classics.application.report.result;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsReportSummaryResult {

    private Instant periodStart;
    private Instant periodEnd;
    private Long contentCount;
    private Long translatedContentCount;
    private Long imageReadyContentCount;
    private Long visualAssetReadyContentCount;
    private List<TopContentResult> topContents;
    private List<ContentGrowthPointResult> contentGrowthSeries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopContentResult {

        private Long contentId;
        private String contentType;
        private String title;
        private Long visitCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentGrowthPointResult {

        private String bucket;
        private Long createdCount;
    }
}
