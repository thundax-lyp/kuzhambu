package com.thundax.kuzhambu.knowledge.application.lineage.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineageCanvasResult {
    private VersionView version;
    private SummaryView summary;
    private List<NodeView> nodes;
    private List<RelationView> relations;
    private NodeView selectedNode;
    private RelationView selectedRelation;
    private AvailableFiltersView availableFilters;
    private EmptyView empty;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionView {
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
    public static class SummaryView {
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
    public static class NodeView {
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
        private List<SourceRefView> sourceRefs;
        private Long firstExtractedAt;
        private Long lastExtractedAt;
        private Double x;
        private Double y;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationView {
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
        private List<SourceRefView> sourceRefs;
        private Long firstExtractedAt;
        private Long lastExtractedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceRefView {
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
    public static class AvailableFiltersView {
        private List<VersionOptionView> versions;
        private List<String> nodeTypes;
        private List<String> relationTypes;
        private List<String> confirmationStatuses;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionOptionView {
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
    public static class EmptyView {
        private String reason;
        private String title;
        private String description;
        private String actionLabel;
        private String actionHref;
    }
}
