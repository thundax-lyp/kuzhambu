package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public final class RefinementRequests {

    private RefinementRequests() {}

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class TaskPageRequest extends PageRequest {
        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("sourceCategoryCode")
        private String sourceCategoryCode;

        @JsonProperty("status")
        private String status;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class TaskOpenRequest {
        @JsonProperty("graphVersionId")
        private Long graphVersionId;

        @JsonProperty("openedBy")
        private Long openedBy;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class TaskDetailRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class TaskApplyRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("appliedBy")
        private Long appliedBy;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class EntityUpsertRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("entityId")
        private Long entityId;

        @JsonProperty("entityKey")
        private String entityKey;

        @JsonProperty("name")
        private String name;

        @JsonProperty("entityType")
        private String entityType;

        @JsonProperty("description")
        private String description;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("sortOrder")
        private Integer sortOrder;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class EntityConfirmRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("entityKey")
        private String entityKey;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class EntityDeleteRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("entityKey")
        private String entityKey;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class RelationUpsertRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("relationId")
        private Long relationId;

        @JsonProperty("relationKey")
        private String relationKey;

        @JsonProperty("sourceEntityKey")
        private String sourceEntityKey;

        @JsonProperty("targetEntityKey")
        private String targetEntityKey;

        @JsonProperty("sourceName")
        private String sourceName;

        @JsonProperty("targetName")
        private String targetName;

        @JsonProperty("relationType")
        private String relationType;

        @JsonProperty("evidence")
        private String evidence;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("sortOrder")
        private Integer sortOrder;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class RelationConfirmRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("relationKey")
        private String relationKey;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class RelationDeleteRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("relationKey")
        private String relationKey;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class LineageNodeUpsertRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

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

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("sortOrder")
        private Integer sortOrder;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class LineageNodeConfirmRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("nodeKey")
        private String nodeKey;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class LineageNodeDeleteRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("nodeKey")
        private String nodeKey;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class LineageRelationUpsertRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("relationId")
        private Long relationId;

        @JsonProperty("relationKey")
        private String relationKey;

        @JsonProperty("sourceNodeKey")
        private String sourceNodeKey;

        @JsonProperty("targetNodeKey")
        private String targetNodeKey;

        @JsonProperty("sourceName")
        private String sourceName;

        @JsonProperty("targetName")
        private String targetName;

        @JsonProperty("relationType")
        private String relationType;

        @JsonProperty("evidence")
        private String evidence;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("sortOrder")
        private Integer sortOrder;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class LineageRelationConfirmRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("relationKey")
        private String relationKey;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class LineageRelationDeleteRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("relationKey")
        private String relationKey;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class AnnotationUpsertRequest {
        @JsonProperty("annotationId")
        private Long annotationId;

        @JsonProperty("objectType")
        private String objectType;

        @JsonProperty("objectKey")
        private String objectKey;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("graphVersionId")
        private Long graphVersionId;

        @JsonProperty("annotationStatus")
        private String annotationStatus;

        @JsonProperty("annotationLabel")
        private String annotationLabel;

        @JsonProperty("comment")
        private String comment;

        @JsonProperty("operatorId")
        private Long operatorId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class AnnotationDeleteRequest {
        @JsonProperty("annotationId")
        private Long annotationId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class AnnotationPageRequest extends PageRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;

        @JsonProperty("objectType")
        private String objectType;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema
    public static class QualitySummaryRequest {
        @JsonProperty("refinementTaskId")
        private Long refinementTaskId;
    }
}
