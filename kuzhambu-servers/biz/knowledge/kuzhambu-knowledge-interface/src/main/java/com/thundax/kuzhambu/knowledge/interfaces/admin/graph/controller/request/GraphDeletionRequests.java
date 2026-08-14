package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

public final class GraphDeletionRequests {

    private GraphDeletionRequests() {}

    @Getter
    @Setter
    @Schema(description = "图谱素材删除变更分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeletionChangePageRequest {

        @Pattern(regexp = "^\\d+$")
        private String pageNo;

        @Pattern(regexp = "^\\d+$")
        private String pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材删除变更决策请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeletionDecisionRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String changeId;

        @NotBlank
        @Pattern(regexp = "PRESERVE_CONTRIBUTION|WITHDRAW_ASSOCIATIONS")
        private String decision;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String lockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材删除任务分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeletionTaskPageRequest {

        @Pattern(regexp = "PRECHECKED|AWAITING_DECISION|PENDING|RUNNING|SUCCEEDED|FAILED")
        private String status;

        @Pattern(regexp = "^\\d+$")
        private String pageNo;

        @Pattern(regexp = "^\\d+$")
        private String pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材删除任务标识请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeletionTaskIdRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String taskId;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材删除任务重试请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeletionTaskRetryRequest extends DeletionTaskIdRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String lockVersion;
    }
}
