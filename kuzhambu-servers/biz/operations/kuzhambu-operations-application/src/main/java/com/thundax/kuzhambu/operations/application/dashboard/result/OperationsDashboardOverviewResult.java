package com.thundax.kuzhambu.operations.application.dashboard.result;

import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import java.math.BigDecimal;
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
public class OperationsDashboardOverviewResult {
    private Date periodStart;
    private Date periodEnd;
    private Long contentCount;
    private Long translatedContentCount;
    private Long imageReadyContentCount;
    private Long visualAssetReadyContentCount;
    private Long shareVisitCount;
    private Long aiInvocationCount;
    private Long aiSucceededInvocationCount;
    private Long aiFailedInvocationCount;
    private BigDecimal aiAvgLatencyMs;
    private BigDecimal aiTotalCostAmount;
    private Long searchCount;
    private Long qaCount;
    private BigDecimal avgSearchLatencyMs;
    private BigDecimal tagCoverageRate;
    private Integer unhealthyComponentCount;
    private Integer runningTaskCount;
    private Integer failedTaskCount;
    private List<BucketCountResult> contentGrowthSeries;
    private List<BucketCountResult> searchTrendSeries;
    private List<BucketCountResult> qaTrendSeries;
    private List<BucketCountResult> tagGrowthSeries;
    private List<OperationsHealthSummaryResult> healthSummaries;
    private List<TaskStatusSummaryResult> taskStatusSummaries;
    private List<TopContentResult> topContents;
    private List<TopQueryResult> topQueries;
    private List<TopTagResult> topTags;
    private List<TopAiCapabilityResult> topAiCapabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BucketCountResult {
        private String bucket;
        private Long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskStatusSummaryResult {
        private String taskStatus;
        private Long count;
    }

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
    public static class TopQueryResult {
        private String queryText;
        private Long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTagResult {
        private String tagName;
        private Long contentRefCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopAiCapabilityResult {
        private String capability;
        private Long invocationCount;
    }
}
