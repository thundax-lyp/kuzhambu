package com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public final class AiInvocationResponses {

    private AiInvocationResponses() {}

    @Getter
    @Builder
    @Schema(name = "AiInvocationIdResponse", description = "AI调用资源ID响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdResponse implements Serializable {

        @JsonProperty(value = "id")
        private Long id;
    }

    @Getter
    @Builder
    @Schema(name = "AiInvocationLogResponse", description = "AI调用记录响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvocationLogResponse implements Serializable {

        @JsonProperty(value = "callId")
        private Long callId;

        @JsonProperty(value = "callIdText")
        private String callIdText;

        @JsonProperty(value = "batchId")
        private Long batchId;

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

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "streamUsed")
        private Boolean streamUsed;

        @JsonProperty(value = "streamCompleted")
        private Boolean streamCompleted;

        @JsonProperty(value = "fallbackUsed")
        private Boolean fallbackUsed;

        @JsonProperty(value = "latencyMs")
        private Integer latencyMs;

        @JsonProperty(value = "inputTokens")
        private Integer inputTokens;

        @JsonProperty(value = "outputTokens")
        private Integer outputTokens;

        @JsonProperty(value = "costAmount")
        private BigDecimal costAmount;

        @JsonProperty(value = "failureStage")
        private String failureStage;

        @JsonProperty(value = "resultFormat")
        private String resultFormat;

        @JsonProperty(value = "errorType")
        private String errorType;

        @JsonProperty(value = "errorMessage")
        private String errorMessage;

        @JsonProperty(value = "warningsJson")
        private String warningsJson;

        @JsonProperty(value = "requestedAt")
        private Instant requestedAt;

        @JsonProperty(value = "completedAt")
        private Instant completedAt;
    }

    @Getter
    @Builder
    @Schema(name = "AiInvocationSummaryResponse", description = "AI调用统计响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvocationSummaryResponse implements Serializable {

        @JsonProperty(value = "periodStart")
        private Instant periodStart;

        @JsonProperty(value = "periodEnd")
        private Instant periodEnd;

        @JsonProperty(value = "invocationCount")
        private Long invocationCount;

        @JsonProperty(value = "succeededInvocationCount")
        private Long succeededInvocationCount;

        @JsonProperty(value = "failedInvocationCount")
        private Long failedInvocationCount;

        @JsonProperty(value = "avgLatencyMs")
        private Long avgLatencyMs;

        @JsonProperty(value = "totalCostAmount")
        private BigDecimal totalCostAmount;

        @JsonProperty(value = "topCapabilities")
        private List<TopCapabilityResponse> topCapabilities;
    }

    @Getter
    @Builder
    @Schema(name = "AiTopCapabilityResponse", description = "AI能力调用排行响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopCapabilityResponse implements Serializable {

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "invocationCount")
        private Long invocationCount;
    }

    @Getter
    @Builder
    @Schema(name = "AiCandidateResponse", description = "AI候选响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateResponse implements Serializable {

        @JsonProperty(value = "candidateId")
        private Long candidateId;

        @JsonProperty(value = "candidateIdText")
        private String candidateIdText;

        @JsonProperty(value = "callId")
        private Long callId;

        @JsonProperty(value = "callIdText")
        private String callIdText;

        @JsonProperty(value = "batchId")
        private Long batchId;

        @JsonProperty(value = "capability")
        private String capability;

        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "contentId")
        private Long contentId;

        @JsonProperty(value = "objectId")
        private Long objectId;

        @JsonProperty(value = "resultFormat")
        private String resultFormat;

        @JsonProperty(value = "resultPayload")
        private String resultPayload;

        @JsonProperty(value = "status")
        private String status;

        @JsonProperty(value = "promptVersionId")
        private Long promptVersionId;

        @JsonProperty(value = "modelName")
        private String modelName;

        @JsonProperty(value = "errorType")
        private String errorType;

        @JsonProperty(value = "errorMessage")
        private String errorMessage;

        @JsonProperty(value = "requestedAt")
        private Instant requestedAt;

        @JsonProperty(value = "appliedAt")
        private Instant appliedAt;
    }

    @Getter
    @Builder
    @Schema(name = "AiBatchJobResponse", description = "AI批量任务响应")
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
