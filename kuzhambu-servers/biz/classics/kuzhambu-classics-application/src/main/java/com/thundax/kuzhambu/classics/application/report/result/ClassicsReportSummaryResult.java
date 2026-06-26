package com.thundax.kuzhambu.classics.application.report.result;

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
public class ClassicsReportSummaryResult {

    private Date periodStart;
    private Date periodEnd;
    private Long contentCount;
    private Long translatedContentCount;
    private Long imageReadyContentCount;
    private Long visualAssetReadyContentCount;
    private Long shareVisitCount;
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
