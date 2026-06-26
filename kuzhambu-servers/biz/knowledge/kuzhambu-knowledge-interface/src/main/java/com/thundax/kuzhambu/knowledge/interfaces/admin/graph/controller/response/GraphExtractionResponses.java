package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

public final class GraphExtractionResponses {

    private GraphExtractionResponses() {}

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskResponse implements Serializable {
        @JsonProperty("taskId")
        private String taskId;

        @JsonProperty("batchJobId")
        private Long batchJobId;

        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("scopeType")
        private String scopeType;

        @JsonProperty("scopeJson")
        private String scopeJson;

        @JsonProperty("triggerSource")
        private String triggerSource;

        @JsonProperty("selectionScopeJson")
        private String selectionScopeJson;

        @JsonProperty("replaceUnconfirmedOnly")
        private Boolean replaceUnconfirmedOnly;

        @JsonProperty("parentTaskId")
        private Long parentTaskId;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("aiCallId")
        private Long aiCallId;

        @JsonProperty("aiCandidateId")
        private Long aiCandidateId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("errorType")
        private String errorType;

        @JsonProperty("errorMessage")
        private String errorMessage;

        @JsonProperty("requestedBy")
        private Long requestedBy;

        @JsonProperty("requestedAt")
        private Long requestedAt;

        @JsonProperty("completedAt")
        private Long completedAt;

        @JsonProperty("appliedAt")
        private Long appliedAt;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchCancelResponse implements Serializable {
        @JsonProperty("batchJobId")
        private Long batchJobId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("cancelledCount")
        private Integer cancelledCount;

        @JsonProperty("completedCount")
        private Integer completedCount;

        @JsonProperty("failedCount")
        private Integer failedCount;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionResponse implements Serializable {
        @JsonProperty("versionId")
        private Long versionId;

        @JsonProperty("taskId")
        private String taskId;

        @JsonProperty("candidateId")
        private Long candidateId;

        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("versionNo")
        private Integer versionNo;

        @JsonProperty("status")
        private String status;

        @JsonProperty("appliedAt")
        private Long appliedAt;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityResponse implements Serializable {
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

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;

        @JsonProperty("latestVersionId")
        private Long latestVersionId;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("firstExtractedAt")
        private Long firstExtractedAt;

        @JsonProperty("lastExtractedAt")
        private Long lastExtractedAt;

        @JsonProperty("confirmedAt")
        private Long confirmedAt;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationResponse implements Serializable {
        @JsonProperty("relationId")
        private Long relationId;

        @JsonProperty("relationKey")
        private String relationKey;

        @JsonProperty("sourceName")
        private String sourceName;

        @JsonProperty("targetName")
        private String targetName;

        @JsonProperty("relationType")
        private String relationType;

        @JsonProperty("evidence")
        private String evidence;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;

        @JsonProperty("latestVersionId")
        private Long latestVersionId;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("firstExtractedAt")
        private Long firstExtractedAt;

        @JsonProperty("lastExtractedAt")
        private Long lastExtractedAt;

        @JsonProperty("confirmedAt")
        private Long confirmedAt;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineageNodeResponse implements Serializable {
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

        @JsonProperty("latestVersionId")
        private Long latestVersionId;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("firstExtractedAt")
        private Long firstExtractedAt;

        @JsonProperty("lastExtractedAt")
        private Long lastExtractedAt;

        @JsonProperty("confirmedAt")
        private Long confirmedAt;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineageRelationResponse implements Serializable {
        @JsonProperty("relationId")
        private Long relationId;

        @JsonProperty("relationKey")
        private String relationKey;

        @JsonProperty("sourceName")
        private String sourceName;

        @JsonProperty("targetName")
        private String targetName;

        @JsonProperty("relationType")
        private String relationType;

        @JsonProperty("evidence")
        private String evidence;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;

        @JsonProperty("latestVersionId")
        private Long latestVersionId;

        @JsonProperty("sourceRefsJson")
        private String sourceRefsJson;

        @JsonProperty("firstExtractedAt")
        private Long firstExtractedAt;

        @JsonProperty("lastExtractedAt")
        private Long lastExtractedAt;

        @JsonProperty("confirmedAt")
        private Long confirmedAt;
    }
}
