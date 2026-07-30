package com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response;

import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthAlertSummaryResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsDashboardOverviewResponse {
    private Instant periodStart;
    private Instant periodEnd;
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
    private Integer activeAlertCount;
    private Integer criticalAlertCount;
    private Integer warningAlertCount;
    private String highestAlertLevel;
    private OperationsHealthAlertSummaryResponse latestAlert;
    private List<BucketCountResponse> contentGrowthSeries;
    private List<BucketCountResponse> searchTrendSeries;
    private List<BucketCountResponse> qaTrendSeries;
    private List<BucketCountResponse> tagGrowthSeries;
    private List<OperationsHealthSummaryResponse> healthSummaries;
    private List<TaskStatusSummaryResponse> taskStatusSummaries;
    private List<TopContentResponse> topContents;
    private List<TopQueryResponse> topQueries;
    private List<TopTagResponse> topTags;
    private List<TopAiCapabilityResponse> topAiCapabilities;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BucketCountResponse {
        private String bucket;
        private Long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskStatusSummaryResponse {
        private String taskStatus;
        private Long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopContentResponse {
        private Long contentId;
        private String contentType;
        private String title;
        private Long visitCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopQueryResponse {
        private String queryText;
        private Long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTagResponse {
        private String tagName;
        private Long contentRefCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopAiCapabilityResponse {
        private String capability;
        private Long invocationCount;
    }
}
