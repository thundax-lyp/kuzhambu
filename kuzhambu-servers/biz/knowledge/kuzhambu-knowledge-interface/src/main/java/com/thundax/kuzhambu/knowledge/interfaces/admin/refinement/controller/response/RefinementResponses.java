package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.response;

import lombok.Builder;
import lombok.Getter;

public final class RefinementResponses {

    private RefinementResponses() {}

    @Getter
    @Builder
    public static class ProgressSummaryResponse {
        private Integer entityPendingCount;
        private Integer entityConfirmedCount;
        private Integer relationPendingCount;
        private Integer relationConfirmedCount;
    }

    @Getter
    @Builder
    public static class WorkbenchItemResponse {
        private Long refinementTaskId;
        private Long graphVersionId;
        private String taskType;
        private String sourceContentType;
        private Long sourceContentId;
        private String sourceCategoryCode;
        private String sourceCategoryName;
        private String status;
        private Long openedBy;
        private Long openedAt;
        private ProgressSummaryResponse progressSummary;
    }

    @Getter
    @Builder
    public static class EntityOptionResponse {
        private String entityKey;
        private String name;
    }

    @Getter
    @Builder
    public static class EntityResponse {
        private Long draftId;
        private Long entityId;
        private String entityKey;
        private String originType;
        private String operationType;
        private String name;
        private String entityType;
        private String description;
        private String confirmationStatus;
        private String sourceRefsJson;
        private Integer sortOrder;
    }

    @Getter
    @Builder
    public static class RelationResponse {
        private Long draftId;
        private Long relationId;
        private String relationKey;
        private String originType;
        private String operationType;
        private String sourceEntityKey;
        private String targetEntityKey;
        private String sourceName;
        private String targetName;
        private String relationType;
        private String evidence;
        private String confirmationStatus;
        private String sourceRefsJson;
        private Integer sortOrder;
    }

    @Getter
    @Builder
    public static class LineageNodeResponse {
        private Long draftId;
        private Long nodeId;
        private String nodeKey;
        private String originType;
        private String operationType;
        private String name;
        private String nodeType;
        private Integer generation;
        private String gender;
        private String confirmationStatus;
        private String sourceRefsJson;
        private Integer sortOrder;
    }

    @Getter
    @Builder
    public static class LineageRelationResponse {
        private Long draftId;
        private Long relationId;
        private String relationKey;
        private String originType;
        private String operationType;
        private String sourceNodeKey;
        private String targetNodeKey;
        private String sourceName;
        private String targetName;
        private String relationType;
        private String evidence;
        private String confirmationStatus;
        private String sourceRefsJson;
        private Integer sortOrder;
    }

    @Getter
    @Builder
    public static class AnnotationResponse {
        private Long annotationId;
        private String objectType;
        private String objectKey;
        private Long graphVersionId;
        private String annotationStatus;
        private String annotationLabel;
        private String comment;
    }

    @Getter
    @Builder
    public static class QualitySummaryResponse {
        private Double entityCoverageRate;
        private Double relationAccuracyRate;
        private Double completenessRate;
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long refinementTaskId;
        private Long graphVersionId;
        private String taskType;
        private String sourceContentType;
        private Long sourceContentId;
        private String sourceCategoryCode;
        private String sourceCategoryName;
        private String status;
        private ProgressSummaryResponse progressSummary;
        private java.util.List<EntityResponse> entities;
        private java.util.List<RelationResponse> relations;
        private java.util.List<LineageNodeResponse> lineageNodes;
        private java.util.List<LineageRelationResponse> lineageRelations;
        private java.util.List<EntityOptionResponse> entityOptions;
    }
}
