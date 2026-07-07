package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public final class AiRefinementResponses {

    private AiRefinementResponses() {}

    @Getter
    @Builder
    @Schema(name = "AiCandidateResultResponse", description = "AI候选结果响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateResultResponse implements Serializable {

        @JsonProperty(value = "callId")
        private Long callId;

        @JsonProperty(value = "candidateId")
        private Long candidateId;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "capability")
        private String capability;

        @Schema(description = "失败阶段；当 status=FAILED 或 PARTIAL 时必须返回页面可读阶段标识")
        @JsonProperty(value = "failureStage")
        private String failureStage;

        @Schema(description = "候选结果格式；TEXT 表示文本候选，STRUCTURED 表示结构化候选")
        @JsonProperty(value = "resultFormat")
        private String resultFormat;

        @Schema(description = "候选结果载荷；正式写回前仅作为候选内容，不直接代表正式业务事实")
        @JsonProperty(value = "resultPayload")
        private String resultPayload;

        @Schema(description = "失败类型；当 status=FAILED 或 PARTIAL 时必须可供页面直接展示或重试分流")
        @JsonProperty(value = "errorType")
        private String errorType;

        @Schema(description = "失败详情；当 status=FAILED 或 PARTIAL 时不得返回空串占位")
        @JsonProperty(value = "errorMessage")
        private String errorMessage;
    }

    @Getter
    @Builder
    @Schema(name = "AiRefinementTaskAcceptedResponse", description = "AI精修任务受理响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskAcceptedResponse implements Serializable {

        @JsonProperty(value = "taskId")
        private Long taskId;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "contentId")
        private Long contentId;

        @JsonProperty(value = "requestedAt")
        private Instant requestedAt;
    }

    @Getter
    @Builder
    @Schema(name = "AiRefinementTaskDetailResponse", description = "AI精修任务详情响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskDetailResponse implements Serializable {

        @JsonProperty(value = "taskId")
        private Long taskId;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "scope")
        private String scope;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "contentId")
        private Long contentId;

        @JsonProperty(value = "objectId")
        private Long objectId;

        @JsonProperty(value = "requestedBy")
        private Long requestedBy;

        @JsonProperty(value = "serviceRole")
        private String serviceRole;

        @JsonProperty(value = "modelId")
        private Long modelId;

        @JsonProperty(value = "modelName")
        private String modelName;

        @JsonProperty(value = "promptVersionId")
        private Long promptVersionId;

        @JsonProperty(value = "requestId")
        private String requestId;

        @JsonProperty(value = "traceId")
        private String traceId;

        @JsonProperty(value = "callId")
        private Long callId;

        @JsonProperty(value = "candidateId")
        private Long candidateId;

        @Schema(description = "失败阶段；当 status=FAILED 或 PARTIAL 时必须返回页面可读阶段标识")
        @JsonProperty(value = "failureStage")
        private String failureStage;

        @Schema(description = "失败类型；用于页面失败提示和重试策略判断")
        @JsonProperty(value = "errorType")
        private String errorType;

        @Schema(description = "失败详情；用于页面直接展示，不得返回空串占位")
        @JsonProperty(value = "errorMessage")
        private String errorMessage;

        @Schema(description = "是否允许前端订阅流式过程")
        @JsonProperty(value = "streamEnabled")
        private Boolean streamEnabled;

        @Schema(description = "结果预览格式；仅表示当前任务结果预览类型，不代表正式写回字段")
        @JsonProperty(value = "resultFormat")
        private String resultFormat;

        @Schema(description = "结果预览内容；用于页面展示任务结果摘要或失败前最后结果")
        @JsonProperty(value = "resultPreview")
        private String resultPreview;

        @JsonProperty(value = "requestedAt")
        private Instant requestedAt;

        @JsonProperty(value = "startedAt")
        private Instant startedAt;

        @JsonProperty(value = "completedAt")
        private Instant completedAt;

        @JsonProperty(value = "cancelledAt")
        private Instant cancelledAt;
    }

    @Getter
    @Builder
    @Schema(name = "AiRefinementTaskPageResponse", description = "AI精修任务分页响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskPageResponse implements Serializable {

        @JsonProperty(value = "items")
        @Builder.Default
        private List<TaskDetailResponse> items = new ArrayList<>();

        @JsonProperty(value = "total")
        private Long total;

        @JsonProperty(value = "pageNo")
        private Integer pageNo;

        @JsonProperty(value = "pageSize")
        private Integer pageSize;
    }

    @Getter
    @Builder
    @Schema(name = "AiRefinementTaskCancelResponse", description = "AI精修任务取消响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskCancelResponse implements Serializable {

        @JsonProperty(value = "taskId")
        private Long taskId;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "cancelledAt")
        private Instant cancelledAt;
    }

    @Getter
    @Builder
    @Schema(name = "AiRefinementBatchJobResponse", description = "AI精修批量任务响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchJobResponse implements Serializable {

        @JsonProperty(value = "batchId")
        private Long batchId;

        @JsonProperty(value = "scope")
        private String scope;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "totalCount")
        private Integer totalCount;

        @JsonProperty(value = "successCount")
        private Integer successCount;

        @JsonProperty(value = "failedCount")
        private Integer failedCount;

        @JsonProperty(value = "cancelledCount")
        private Integer cancelledCount;

        @Schema(description = "批量失败聚合摘要；用于页面展示已失败或已跳过单元的汇总信息")
        @JsonProperty(value = "failureSummaryJson")
        private String failureSummaryJson;

        @JsonProperty(value = "requestedAt")
        private Instant requestedAt;

        @JsonProperty(value = "cancelledAt")
        private Instant cancelledAt;

        @JsonProperty(value = "completedAt")
        private Instant completedAt;
    }
}
