package com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthAlertSummaryResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OperationsDashboardOverviewResponse", description = "Operations 仪表盘概览响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsDashboardOverviewResponse {
    private Instant periodStart;
    private Instant periodEnd;
    private Long contentCount;
    private Long translatedContentCount;
    private Long imageReadyContentCount;
    private Long visualAssetReadyContentCount;
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
    @Builder
    @Schema(name = "BucketCountResponse", description = "聚合指标时间桶")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BucketCountResponse {
        private String bucket;
        private Long count;
    }

    @Getter
    @Builder
    @Schema(name = "TaskStatusSummaryResponse", description = "任务状态统计")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskStatusSummaryResponse {
        private String taskStatus;
        private Long count;
    }

    @Getter
    @Builder
    @Schema(name = "TopContentResponse", description = "Top 内容")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopContentResponse {
        private Long contentId;
        private String contentType;
        private String title;
        private Long visitCount;
    }

    @Getter
    @Builder
    @Schema(name = "TopQueryResponse", description = "Top 查询词")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopQueryResponse {
        private String queryText;
        private Long count;
    }

    @Getter
    @Builder
    @Schema(name = "TopTagResponse", description = "Top 标签")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopTagResponse {
        private String tagName;
        private Long contentRefCount;
    }

    @Getter
    @Builder
    @Schema(name = "TopAiCapabilityResponse", description = "Top AI 能力")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopAiCapabilityResponse {
        private String capability;
        private Long invocationCount;
    }
}
