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

public final class GraphPublishedRequests {

    private GraphPublishedRequests() {}

    @Getter
    @Setter
    @Schema(description = "图谱发布节点分页查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodePageRequest {

        @Size(max = 128)
        private String keyword;

        @Size(max = 64)
        private String nodeType;

        @Pattern(regexp = "ACTIVE|DELETED")
        private String status;

        @Pattern(regexp = "MATERIAL|MANUAL")
        private String source;

        @Pattern(regexp = "^\\d+$")
        private String pageNo;

        @Pattern(regexp = "^\\d+$")
        private String pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布关系分页查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedEdgePageRequest {

        @Size(max = 128)
        private String keyword;

        @Size(max = 64)
        private String relationType;

        @Pattern(regexp = "ACTIVE|DELETED")
        private String status;

        @Pattern(regexp = "MATERIAL|MANUAL")
        private String source;

        @Pattern(regexp = "^\\d+$")
        private String pageNo;

        @Pattern(regexp = "^\\d+$")
        private String pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布空间单跳邻接分页查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedAdjacencyPageRequest {

        @Size(max = 128)
        private String subjectKeyword;

        @Size(max = 64)
        private String subjectType;

        @Pattern(regexp = "ACTIVE|DELETED")
        private String subjectStatus;

        @Pattern(regexp = "MATERIAL|MANUAL")
        private String subjectSource;

        @Size(max = 64)
        private String relationType;

        @Pattern(regexp = "ACTIVE|DELETED")
        private String relationStatus;

        @Pattern(regexp = "MATERIAL|MANUAL")
        private String relationSource;

        @Size(max = 128)
        private String objectKeyword;

        @Size(max = 64)
        private String objectType;

        @Pattern(regexp = "ACTIVE|DELETED")
        private String objectStatus;

        @Pattern(regexp = "MATERIAL|MANUAL")
        private String objectSource;

        private Boolean includeIsolated;

        @Pattern(regexp = "^\\d+$")
        private String pageNo;

        @Pattern(regexp = "^\\d+$")
        private String pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布节点标识请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedIdRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String nodeId;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布关系标识请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedEdgeIdRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String edgeId;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布节点创建或更新请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodeSaveRequest {

        @Valid
        @NotNull
        private PublishedNodeRequestData node;

        @NotNull
        @Valid
        private List<PublishedPropertyRequestData> properties;

        @NotBlank
        @Size(max = 1024)
        private String reason;

        @Pattern(regexp = "^\\d+$")
        private String lockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布关系创建或更新请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedEdgeSaveRequest {

        @Valid
        @NotNull
        private PublishedEdgeRequestData edge;

        @NotNull
        @Valid
        private List<PublishedPropertyRequestData> properties;

        @NotBlank
        @Size(max = 1024)
        private String reason;

        @Pattern(regexp = "^\\d+$")
        private String lockVersion;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布节点删除影响预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodeDeletePreviewRequest extends PublishedIdRequest {

        @NotNull
        private Boolean cascadeEdges;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布节点删除确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodeDeleteRequest extends PublishedNodeDeletePreviewRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String lockVersion;

        @NotBlank
        private String impactToken;

        @NotBlank
        @Size(max = 1024)
        private String reason;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布节点合并影响预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodeMergePreviewRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String retainedNodeId;

        @NotEmpty
        private List<@Pattern(regexp = "^\\d+$") String> mergedNodeIds;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布节点合并确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodeMergeRequest extends PublishedNodeMergePreviewRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String retainedNodeLockVersion;

        @NotBlank
        private String impactToken;

        @NotBlank
        @Size(max = 1024)
        private String reason;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布节点拆分影响预览请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodeSplitPreviewRequest extends PublishedIdRequest {}

    @Getter
    @Setter
    @Schema(description = "图谱发布节点拆分确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodeSplitRequest extends PublishedNodeSplitPreviewRequest {

        @Valid
        @NotNull
        private PublishedNodeRequestData splitNode;

        private List<@Pattern(regexp = "^\\d+$") String> movedPropertyIds;

        private List<@Pattern(regexp = "^\\d+$") String> copiedPropertyIds;

        private List<@Pattern(regexp = "^\\d+$") String> reassignedEdgeIds;

        @Valid
        private List<PublishedEdgeRequestData> copiedEdges;

        @Valid
        private List<GraphMaterialRequests.ContentRefRequest> movedMaterialRefs;

        @Valid
        private List<GraphMaterialRequests.ContentRefRequest> copiedMaterialRefs;

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String sourceNodeLockVersion;

        @NotBlank
        private String impactToken;

        @NotBlank
        @Size(max = 1024)
        private String reason;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布关系删除确认请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedEdgeDeleteRequest extends PublishedEdgeIdRequest {

        @NotBlank
        @Pattern(regexp = "^\\d+$")
        private String lockVersion;

        @NotBlank
        private String impactToken;

        @NotBlank
        @Size(max = 1024)
        private String reason;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布节点协议对象")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedNodeRequestData {

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

        @NotBlank
        @Pattern(regexp = "ACTIVE|DELETED")
        private String status;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布关系协议对象")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedEdgeRequestData {

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

        @NotBlank
        @Pattern(regexp = "ACTIVE|DELETED")
        private String status;
    }

    @Getter
    @Setter
    @Schema(description = "图谱发布属性协议对象")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishedPropertyRequestData {

        @Pattern(regexp = "^\\d+$")
        private String id;

        @NotBlank
        @Size(max = 128)
        private String propertyName;

        @NotNull
        private Object value;

        @NotNull
        private Boolean preferred;
    }
}
