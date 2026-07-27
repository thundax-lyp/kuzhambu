package com.thundax.kuzhambu.ai.interfaces.admin.invocation.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

public final class AiInvocationRequests {

    private AiInvocationRequests() {}

    @Getter
    @Setter
    @Schema(name = "AiCallIdRequest", description = "AI调用ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallIdRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "callId")
        private Long callId;
    }

    @Getter
    @Setter
    @Schema(name = "AiInvocationLogPageRequest", description = "AI调用记录分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvocationLogPageRequest extends PageRequest {

        @Size(max = 64)
        @JsonProperty(value = "scope")
        private String scope;

        @Size(max = 64)
        @JsonProperty(value = "capability")
        private String capability;

        @Size(max = 64)
        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "contentId")
        private Long contentId;

        @Size(max = 32)
        @JsonProperty(value = "status")
        private String status;

        @Size(max = 32)
        @JsonProperty(value = "serviceRole")
        private String serviceRole;

        @Size(max = 128)
        @JsonProperty(value = "modelName")
        private String modelName;

        @JsonProperty(value = "fallbackUsed")
        private Boolean fallbackUsed;

        @JsonProperty(value = "requestedAtStart")
        private Instant requestedAtStart;

        @JsonProperty(value = "requestedAtEnd")
        private Instant requestedAtEnd;
    }

    @Getter
    @Setter
    @Schema(name = "AiInvocationSummaryRequest", description = "AI调用统计请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvocationSummaryRequest implements Serializable {

        @JsonProperty(value = "periodStart")
        private Instant periodStart;

        @JsonProperty(value = "periodEnd")
        private Instant periodEnd;

        @Size(max = 32)
        @JsonProperty(value = "bucketType")
        private String bucketType;

        @Size(max = 64)
        @JsonProperty(value = "scope")
        private String scope;

        @Size(max = 64)
        @JsonProperty(value = "capability")
        private String capability;

        @Size(max = 32)
        @JsonProperty(value = "serviceRole")
        private String serviceRole;
    }

    @Getter
    @Setter
    @Schema(name = "AiCandidateIdRequest", description = "AI候选ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateIdRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "candidateId")
        private Long candidateId;
    }

    @Getter
    @Setter
    @Schema(name = "AiCandidateListRequest", description = "AI候选列表请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateListRequest implements Serializable {

        @Size(max = 64)
        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "contentId")
        private Long contentId;

        @JsonProperty(value = "objectId")
        private Long objectId;

        @Size(max = 64)
        @JsonProperty(value = "capability")
        private String capability;

        @Size(max = 32)
        @JsonProperty(value = "status")
        private String status;
    }

    @Getter
    @Setter
    @Schema(name = "AiCandidateRejectRequest", description = "AI候选拒绝请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateRejectRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "candidateId")
        private Long candidateId;

        @NotBlank
        @Size(max = 64)
        @JsonProperty(value = "errorType")
        private String errorType;

        @Size(max = 500)
        @JsonProperty(value = "errorMessage")
        private String errorMessage;
    }

    @Getter
    @Setter
    @Schema(name = "AiCandidateMarkAppliedRequest", description = "AI候选标记已应用请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateMarkAppliedRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "candidateId")
        private Long candidateId;

        @Size(max = 64)
        @JsonProperty(value = "resultFormat")
        private String resultFormat;

        @JsonProperty(value = "resultPayload")
        private String resultPayload;
    }

    @Getter
    @Setter
    @Schema(name = "AiBatchIdRequest", description = "AI批量任务ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchIdRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "batchId")
        private Long batchId;
    }

    @Getter
    @Setter
    @Schema(name = "AiBatchCreateRequest", description = "AI批量任务创建请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchCreateRequest implements Serializable {

        @NotBlank
        @Size(max = 64)
        @JsonProperty(value = "scope")
        private String scope;

        @NotBlank
        @Size(max = 64)
        @JsonProperty(value = "capability")
        private String capability;

        @NotBlank
        @Size(max = 64)
        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "totalCount")
        private int totalCount;

        @JsonProperty(value = "failureSummaryJson")
        private String failureSummaryJson;
    }

    @Getter
    @Setter
    @Schema(name = "AiBatchFailureRequest", description = "AI批量失败记录请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchFailureRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "batchId")
        private Long batchId;

        @JsonProperty(value = "failureSummaryJson")
        private String failureSummaryJson;
    }
}
