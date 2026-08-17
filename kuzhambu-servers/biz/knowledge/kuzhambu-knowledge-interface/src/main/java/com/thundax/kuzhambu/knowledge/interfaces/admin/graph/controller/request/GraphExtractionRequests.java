package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class GraphExtractionRequests {

    private GraphExtractionRequests() {}

    @Getter
    @Setter
    @Schema(description = "图谱提取创建请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractionCreateRequest extends GraphMaterialRequests.ContentRefRequest {

        @NotBlank
        @Size(max = 128)
        private String idempotencyKey;
    }

    @Getter
    @Setter
    @Schema(description = "图谱批量提取创建请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchCreateRequest {

        @Valid
        private SelectionData selection;

        @NotBlank
        @Size(max = 128)
        private String idempotencyKey;
    }

    @Getter
    @Setter
    @Schema(description = "图谱批量提取选择")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SelectionData {

        @Valid
        private List<GraphMaterialRequests.ContentRefRequest> contentRefs;

        @Size(max = 64)
        private String volumeCode;
    }

    @Getter
    @Setter
    @Schema(description = "图谱任务分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskPageRequest {

        @Size(max = 128)
        private String keyword;

        @Size(max = 32)
        private String contentType;

        @Size(max = 64)
        private String categoryCode;

        @Size(max = 64)
        private String volumeCode;

        @Valid
        private List<GraphMaterialRequests.ContentRefRequest> contentRefs;

        @Size(max = 128)
        private String batchId;

        @Size(max = 32)
        private String executionStatus;

        @Size(max = 32)
        private String disposition;

        @Pattern(regexp = "NONE|MATERIAL")
        private String groupBy;

        @Pattern(regexp = "^\\d+$")
        private String pageNo;

        @Pattern(regexp = "^\\d+$")
        private String pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱任务详情请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskGetRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String taskId;
    }

    @Getter
    @Setter
    @Schema(description = "图谱任务动作请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskActionRequest extends TaskGetRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String taskLockVersion;

        @NotBlank
        @Size(max = 32)
        private String expectedExecutionStatus;

        @NotBlank
        @Size(max = 128)
        private String idempotencyKey;
    }

    @Getter
    @Setter
    @Schema(description = "图谱候选采用请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateApplyRequest extends TaskActionRequest {

        @NotBlank
        @Size(max = 32)
        private String expectedDisposition;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;

        @NotBlank
        @Pattern(regexp = "MERGE|REPLACE")
        private String applyMode;
    }

    @Getter
    @Setter
    @Schema(description = "图谱候选丢弃请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateDiscardRequest extends TaskActionRequest {

        @NotBlank
        @Size(max = 32)
        private String expectedDisposition;

        @Size(max = 512)
        private String reason;
    }

    @Getter
    @Setter
    @Schema(description = "图谱候选重新生成请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateRegenerateRequest extends TaskActionRequest {

        @Size(max = 32)
        private String expectedDisposition;
    }
}
