package com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "KnowledgePortalLineageResponse", description = "Portal世系查询响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgePortalLineageResponse {
    @Schema(name = "version", description = "当前版本信息")
    @JsonProperty("version")
    private VersionResponse version;

    @Schema(name = "summary", description = "汇总统计")
    @JsonProperty("summary")
    private SummaryResponse summary;

    @Schema(name = "nodes", description = "节点列表")
    @JsonProperty("nodes")
    private List<NodeResponse> nodes;

    @Schema(name = "relations", description = "关系列表")
    @JsonProperty("relations")
    private List<RelationResponse> relations;

    @Schema(name = "selectedNode", description = "选中节点")
    @JsonProperty("selectedNode")
    private NodeResponse selectedNode;

    @Schema(name = "selectedRelation", description = "选中关系")
    @JsonProperty("selectedRelation")
    private RelationResponse selectedRelation;

    @Schema(name = "availableFilters", description = "可用过滤条件")
    @JsonProperty("availableFilters")
    private AvailableFiltersResponse availableFilters;

    @Schema(name = "empty", description = "空状态")
    @JsonProperty("empty")
    private EmptyResponse empty;

    @Getter
    @Builder
    @Schema(name = "VersionResponse", description = "版本信息")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionResponse {
        @JsonProperty("versionId")
        private Long versionId;

        @JsonProperty("versionNo")
        private Integer versionNo;

        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("status")
        private String status;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("sourceCategoryCode")
        private String sourceCategoryCode;

        @JsonProperty("sourceCategoryName")
        private String sourceCategoryName;

        @JsonProperty("appliedAt")
        private Long appliedAt;
    }

    @Getter
    @Builder
    @Schema(name = "SummaryResponse", description = "汇总统计")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SummaryResponse {
        @JsonProperty("nodeCount")
        private Long nodeCount;

        @JsonProperty("relationCount")
        private Long relationCount;

        @JsonProperty("confirmedNodeCount")
        private Long confirmedNodeCount;

        @JsonProperty("confirmedRelationCount")
        private Long confirmedRelationCount;

        @JsonProperty("focusNodeId")
        private Long focusNodeId;

        @JsonProperty("focusRelationId")
        private Long focusRelationId;
    }

    @Getter
    @Builder
    @Schema(name = "NodeResponse", description = "节点信息")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeResponse {
        @JsonProperty("id")
        private String id;

        @JsonProperty("nodeId")
        private Long nodeId;

        @JsonProperty("nodeKey")
        private String nodeKey;

        @JsonProperty("name")
        private String name;

        @JsonProperty("nodeType")
        private String nodeType;

        @JsonProperty("generation")
        private Integer generation;

        @JsonProperty("gender")
        private String gender;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;

        @JsonProperty("confidence")
        private Double confidence;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("sourceRefs")
        private List<SourceRefResponse> sourceRefs;

        @JsonProperty("firstExtractedAt")
        private Long firstExtractedAt;

        @JsonProperty("lastExtractedAt")
        private Long lastExtractedAt;

        @JsonProperty("x")
        private Double x;

        @JsonProperty("y")
        private Double y;
    }

    @Getter
    @Builder
    @Schema(name = "RelationResponse", description = "关系信息")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationResponse {
        @JsonProperty("id")
        private String id;

        @JsonProperty("relationId")
        private Long relationId;

        @JsonProperty("sourceNodeId")
        private Long sourceNodeId;

        @JsonProperty("sourceNodeName")
        private String sourceNodeName;

        @JsonProperty("targetNodeId")
        private Long targetNodeId;

        @JsonProperty("targetNodeName")
        private String targetNodeName;

        @JsonProperty("relationType")
        private String relationType;

        @JsonProperty("relationLabel")
        private String relationLabel;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;

        @JsonProperty("confidence")
        private Double confidence;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("sourceRefs")
        private List<SourceRefResponse> sourceRefs;

        @JsonProperty("firstExtractedAt")
        private Long firstExtractedAt;

        @JsonProperty("lastExtractedAt")
        private Long lastExtractedAt;
    }

    @Getter
    @Builder
    @Schema(name = "SourceRefResponse", description = "来源引用")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceRefResponse {
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("sourceTitle")
        private String sourceTitle;

        @JsonProperty("snippet")
        private String snippet;

        @JsonProperty("href")
        private String href;
    }

    @Getter
    @Builder
    @Schema(name = "AvailableFiltersResponse", description = "可用过滤器")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvailableFiltersResponse {
        @JsonProperty("versions")
        private List<VersionResponse> versions;

        @JsonProperty("nodeTypes")
        private List<String> nodeTypes;

        @JsonProperty("relationTypes")
        private List<String> relationTypes;

        @JsonProperty("confirmationStatuses")
        private List<String> confirmationStatuses;
    }

    @Getter
    @Builder
    @Schema(name = "EmptyResponse", description = "空结果")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmptyResponse {
        @JsonProperty("reason")
        private String reason;

        @JsonProperty("title")
        private String title;

        @JsonProperty("description")
        private String description;

        @JsonProperty("actionLabel")
        private String actionLabel;

        @JsonProperty("actionHref")
        private String actionHref;
    }
}
