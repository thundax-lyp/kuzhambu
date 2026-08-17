package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class GraphPublicationRequests {

    private GraphPublicationRequests() {}

    @Getter
    @Setter
    @Schema(description = "图谱素材发布预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicationPreviewRequest extends GraphMaterialRequests.ContentRefRequest {}

    @Getter
    @Setter
    @Schema(description = "图谱素材发布确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicationConfirmRequest extends GraphMaterialRequests.ContentRefRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$")
        private String previewToken;

        @Valid
        private List<PublicationConflictDecisionRequest> conflictDecisions;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材批量发布预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchPublicationPreviewRequest {

        @NotEmpty
        @Valid
        private List<GraphMaterialRequests.ContentRefRequest> contentRefs;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材批量发布确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchPublicationConfirmRequest {

        @NotEmpty
        @Valid
        private List<PublicationConfirmRequest> materials;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材撤回请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WithdrawalRequest extends GraphMaterialRequests.ContentRefRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材批量撤回预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchWithdrawalPreviewRequest {

        @NotEmpty
        @Valid
        private List<GraphMaterialRequests.ContentRefRequest> contentRefs;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材批量撤回请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchWithdrawalRequest {

        @NotEmpty
        @Valid
        private List<WithdrawalRequest> materials;

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,128}$")
        private String idempotencyKey;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材发布冲突处理决策")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicationConflictDecisionRequest {

        @NotBlank
        @Pattern(regexp = "NODE|EDGE")
        private String objectType;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialObjectId;

        @NotBlank
        @Pattern(regexp = "REUSE_MATCH|CREATE_NEW")
        private String action;

        @Pattern(regexp = "^\\d+$")
        private String matchedObjectId;
    }
}
