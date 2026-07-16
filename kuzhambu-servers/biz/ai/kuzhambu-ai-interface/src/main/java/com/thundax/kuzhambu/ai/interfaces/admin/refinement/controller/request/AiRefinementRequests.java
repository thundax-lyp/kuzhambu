package com.thundax.kuzhambu.ai.interfaces.admin.refinement.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public final class AiRefinementRequests {

    private AiRefinementRequests() {}

    @Getter
    @Setter
    @Schema(name = "AiRefinementRequest", description = "AI精修请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RefinementRequest implements Serializable {

        @Schema(description = "能力编码；三才视觉资产场景固定使用 image_analysis、fusion、visual、image_gen")
        @Size(max = 64)
        @JsonProperty(value = "capability")
        private String capability;

        @NotBlank
        @Size(max = 64)
        @JsonProperty(value = "scope")
        private String scope;

        @Size(max = 64)
        @JsonProperty(value = "operation")
        private String operation;

        @Schema(description = "业务内容类型；三才视觉资产场景固定为 SANCAI_ENTRY")
        @NotBlank
        @Size(max = 64)
        @JsonProperty(value = "contentType")
        private String contentType;

        @NotNull
        @JsonProperty(value = "contentId")
        private Long contentId;

        @Schema(description = "业务对象标识；三才视觉资产场景固定传 visualAssetId")
        @JsonProperty(value = "objectId")
        private Long objectId;

        @JsonProperty(value = "requestedBy")
        private Long requestedBy;

        @JsonProperty(value = "serviceId")
        private Long serviceId;

        @Size(max = 32)
        @JsonProperty(value = "serviceRole")
        private String serviceRole;

        @JsonProperty(value = "modelId")
        private Long modelId;

        @Size(max = 128)
        @JsonProperty(value = "modelName")
        private String modelName;

        @JsonProperty(value = "promptVersionId")
        private Long promptVersionId;

        @NotBlank
        @Size(max = 128)
        @JsonProperty(value = "requestId")
        private String requestId;

        @NotBlank
        @Size(max = 128)
        @JsonProperty(value = "traceId")
        private String traceId;

        @NotBlank
        @JsonProperty(value = "promptMessagesJson")
        private String promptMessagesJson;

        @JsonProperty(value = "promptVariablesJson")
        private String promptVariablesJson;

        @Size(max = 128)
        @JsonProperty(value = "promptHash")
        private String promptHash;

        @Schema(description = "AI 输入上下文 JSON；三才视觉资产场景必须包含 entryId、visualAssetId 和 capability 上下文")
        @NotBlank
        @JsonProperty(value = "inputPayloadJson")
        private String inputPayloadJson;

        @JsonProperty(value = "outputSchemaJson")
        private String outputSchemaJson;

        @JsonProperty(value = "forceJson")
        private Boolean forceJson;

        @Size(max = 32)
        @JsonProperty(value = "locale")
        private String locale;
    }

    @Getter
    @Setter
    @Schema(name = "AiRefinementTaskIdRequest", description = "AI精修任务ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskIdRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "taskId")
        private Long taskId;
    }

    @Getter
    @Setter
    @Schema(name = "AiRefinementTaskCancelRequest", description = "AI精修任务取消请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskCancelRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "taskId")
        private Long taskId;

        @NotNull
        @JsonProperty(value = "requestedBy")
        private Long requestedBy;
    }

    @Getter
    @Setter
    @Schema(name = "AiRefinementTaskPageRequest", description = "AI精修任务分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskPageRequest extends PageRequest {

        @Size(max = 64)
        @JsonProperty(value = "capability")
        private String capability;

        @Size(max = 32)
        @JsonProperty(value = "status")
        private String status;

        @Size(max = 64)
        @JsonProperty(value = "contentType")
        private String contentType;

        @JsonProperty(value = "contentId")
        private Long contentId;

        @JsonProperty(value = "requestedBy")
        private Long requestedBy;
    }

    @Getter
    @Setter
    @Schema(name = "AiRefinementBatchCreateRequest", description = "AI精修批量任务创建请求")
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

        @NotNull
        @Positive
        @JsonProperty(value = "totalCount")
        private Integer totalCount;

        @JsonProperty(value = "failureSummaryJson")
        private String failureSummaryJson;
    }

    @Getter
    @Setter
    @Schema(name = "AiRefinementBatchIdRequest", description = "AI精修批量任务ID请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchIdRequest implements Serializable {

        @NotNull
        @JsonProperty(value = "batchId")
        private Long batchId;
    }
}
