package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public final class GraphMaterialRequests {

    private GraphMaterialRequests() {}

    @Getter
    @Setter
    @Schema(description = "图谱素材内容引用请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentRefRequest {

        @NotBlank
        @Size(max = 64)
        private String contentType;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String contentRefId;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材分页查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialPageRequest {

        @Size(max = 128)
        private String keyword;

        @Pattern(regexp = "DRAFT|PUBLISHING|PUBLISHED|WITHDRAWING|FAILED")
        private String status;

        @Size(max = 32)
        private String contentType;

        @Size(max = 64)
        private String categoryCode;

        @Size(max = 64)
        private String volumeCode;

        @Pattern(regexp = "^\\d+$")
        private String pageNo;

        @Pattern(regexp = "^\\d+$")
        private String pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材节点创建或更新请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialObjectRequest extends ContentRefRequest {

        @Valid
        @NotNull
        private MaterialObjectRequestData node;

        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材关系创建或更新请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialEdgeRequest extends ContentRefRequest {

        @Valid
        @NotNull
        private MaterialEdgeRequestData edge;

        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材节点删除请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialObjectDeleteRequest extends ContentRefRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String nodeId;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材关系删除请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialEdgeDeleteRequest extends ContentRefRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String edgeId;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材节点合并预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialNodeMergePreviewRequest extends ContentRefRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String retainedNodeId;

        @NotEmpty
        private List<@Pattern(regexp = "^\\d+$") String> mergedNodeIds;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材节点合并确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialNodeMergeApplyRequest extends MaterialNodeMergePreviewRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材节点拆分预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialNodeSplitPreviewRequest extends ContentRefRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String sourceNodeId;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材节点拆分确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialNodeSplitApplyRequest extends MaterialNodeSplitPreviewRequest {

        @Valid
        @NotNull
        private MaterialObjectRequestData splitNode;

        private List<@Pattern(regexp = "^\\d+$") String> reassignedEdgeIds;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材抽取失败重试请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractionRetryRequest extends ContentRefRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String failedTaskId;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材 JSON 导入预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialImportPreviewRequest extends ContentRefRequest {

        @NotBlank
        private String graphJson;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材 JSON 导入确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialImportApplyRequest extends MaterialImportPreviewRequest {

        @NotBlank
        @Pattern(regexp = "MERGE|REPLACE")
        private String applyMode;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String materialLockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材节点协议对象")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialObjectRequestData {

        @Pattern(regexp = "^\\d+$")
        private String id;

        @NotBlank
        @Size(max = 64)
        private String nodeType;

        @NotBlank
        @Size(max = 256)
        private String name;

        @NotNull
        private Map<String, Object> properties;

        @NotBlank
        @Pattern(regexp = "MATERIAL|MANUAL")
        private String source;
    }

    @Getter
    @Setter
    @Schema(description = "图谱素材关系协议对象")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaterialEdgeRequestData {

        @Pattern(regexp = "^\\d+$")
        private String id;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String sourceNodeId;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String targetNodeId;

        @NotBlank
        @Size(max = 64)
        private String relationType;

        @NotNull
        private Map<String, Object> qualifiers;

        @NotBlank
        @Pattern(regexp = "MATERIAL|MANUAL")
        private String source;
    }
}
