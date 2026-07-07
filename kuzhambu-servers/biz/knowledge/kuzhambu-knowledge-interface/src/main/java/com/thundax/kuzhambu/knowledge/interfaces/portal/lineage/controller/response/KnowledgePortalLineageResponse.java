package com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgePortalLineageResponse {
    private VersionResponse version;
    private SummaryResponse summary;
    private List<NodeResponse> nodes;
    private List<RelationResponse> relations;
    private NodeResponse selectedNode;
    private RelationResponse selectedRelation;
    private AvailableFiltersResponse availableFilters;
    private EmptyResponse empty;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionResponse {
        private Long versionId;
        private Integer versionNo;
        private String taskType;
        private String status;
        private String sourceContentType;
        private Long sourceContentId;
        private String sourceCategoryCode;
        private String sourceCategoryName;
        private Long appliedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SummaryResponse {
        private Long nodeCount;
        private Long relationCount;
        private Long confirmedNodeCount;
        private Long confirmedRelationCount;
        private Long focusNodeId;
        private Long focusRelationId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeResponse {
        private String id;
        private Long nodeId;
        private String nodeKey;
        private String name;
        private String nodeType;
        private Integer generation;
        private String gender;
        private String confirmationStatus;
        private Double confidence;
        private String sourceRefsJson;
        private List<SourceRefResponse> sourceRefs;
        private Long firstExtractedAt;
        private Long lastExtractedAt;
        private Double x;
        private Double y;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationResponse {
        private String id;
        private Long relationId;
        private Long sourceNodeId;
        private String sourceNodeName;
        private Long targetNodeId;
        private String targetNodeName;
        private String relationType;
        private String relationLabel;
        private String confirmationStatus;
        private Double confidence;
        private String sourceRefsJson;
        private List<SourceRefResponse> sourceRefs;
        private Long firstExtractedAt;
        private Long lastExtractedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceRefResponse {
        private String sourceContentType;
        private Long sourceContentId;
        private String sourceTitle;
        private String snippet;
        private String href;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvailableFiltersResponse {
        private List<VersionResponse> versions;
        private List<String> nodeTypes;
        private List<String> relationTypes;
        private List<String> confirmationStatuses;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmptyResponse {
        private String reason;
        private String title;
        private String description;
        private String actionLabel;
        private String actionHref;
    }
}
