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

        @JsonProperty(value = "failureStage")
        private String failureStage;

        @JsonProperty(value = "resultFormat")
        private String resultFormat;

        @JsonProperty(value = "resultPayload")
        private String resultPayload;

        @JsonProperty(value = "errorType")
        private String errorType;

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

        @JsonProperty(value = "failureStage")
        private String failureStage;

        @JsonProperty(value = "errorType")
        private String errorType;

        @JsonProperty(value = "errorMessage")
        private String errorMessage;

        @JsonProperty(value = "resultFormat")
        private String resultFormat;

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
}
