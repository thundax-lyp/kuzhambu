package com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineageCanvasResponse implements Serializable {

    @JsonProperty("version")
    private VersionResponse version;

    @JsonProperty("summary")
    private SummaryResponse summary;

    @JsonProperty("nodes")
    private List<NodeResponse> nodes;

    @JsonProperty("relations")
    private List<RelationResponse> relations;

    @JsonProperty("selectedNode")
    private NodeResponse selectedNode;

    @JsonProperty("selectedRelation")
    private RelationResponse selectedRelation;

    @JsonProperty("availableFilters")
    private AvailableFiltersResponse availableFilters;

    @JsonProperty("empty")
    private EmptyResponse empty;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionResponse implements Serializable {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SummaryResponse implements Serializable {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeResponse implements Serializable {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationResponse implements Serializable {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceRefResponse implements Serializable {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvailableFiltersResponse implements Serializable {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmptyResponse implements Serializable {
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
