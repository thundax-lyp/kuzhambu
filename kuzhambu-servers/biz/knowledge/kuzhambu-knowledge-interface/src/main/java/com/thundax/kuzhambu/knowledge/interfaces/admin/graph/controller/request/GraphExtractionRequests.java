package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import lombok.Getter;
import lombok.Setter;

public final class GraphExtractionRequests {

    private GraphExtractionRequests() {}

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateRequest {
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

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("requestedBy")
        private Long requestedBy;

        @JsonProperty("serviceId")
        private Long serviceId;

        @JsonProperty("serviceRole")
        private String serviceRole;

        @JsonProperty("modelId")
        private Long modelId;

        @JsonProperty("modelName")
        private String modelName;

        @JsonProperty("promptVersionId")
        private Long promptVersionId;

        @JsonProperty("requestId")
        private String requestId;

        @JsonProperty("traceId")
        private String traceId;

        @JsonProperty("promptMessagesJson")
        private String promptMessagesJson;

        @JsonProperty("promptVariablesJson")
        private String promptVariablesJson;

        @JsonProperty("promptHash")
        private String promptHash;

        @JsonProperty("inputPayloadJson")
        private String inputPayloadJson;

        @JsonProperty("outputSchemaJson")
        private String outputSchemaJson;

        @JsonProperty("forceJson")
        private Boolean forceJson;

        @JsonProperty("locale")
        private String locale;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageTaskRequest extends PageRequest {
        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("batchJobId")
        private Long batchJobId;

        @JsonProperty("triggerSource")
        private String triggerSource;

        @JsonProperty("status")
        private String status;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskIdRequest {
        @JsonProperty("taskId")
        private Long taskId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegenerateRequest {
        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("sourceTaskId")
        private Long sourceTaskId;

        @JsonProperty("triggerSource")
        private String triggerSource;

        @JsonProperty("selectionScopeJson")
        private String selectionScopeJson;

        @JsonProperty("replaceUnconfirmedOnly")
        private Boolean replaceUnconfirmedOnly;

        @JsonProperty("requestedBy")
        private Long requestedBy;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchCancelRequest {
        @JsonProperty("batchJobId")
        private Long batchJobId;

        @JsonProperty("requestedBy")
        private Long requestedBy;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionPageRequest extends PageRequest {
        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("status")
        private String status;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionIdRequest {
        @JsonProperty("versionId")
        private Long versionId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityPageRequest extends PageRequest {
        @JsonProperty("versionId")
        private Long versionId;

        @JsonProperty("keyword")
        private String keyword;

        @JsonProperty("entityType")
        private String entityType;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityIdRequest {
        @JsonProperty("entityId")
        private Long entityId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationPageRequest extends PageRequest {
        @JsonProperty("versionId")
        private Long versionId;

        @JsonProperty("keyword")
        private String keyword;

        @JsonProperty("relationType")
        private String relationType;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationIdRequest {
        @JsonProperty("relationId")
        private Long relationId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineageNodePageRequest extends PageRequest {
        @JsonProperty("versionId")
        private Long versionId;

        @JsonProperty("keyword")
        private String keyword;

        @JsonProperty("nodeType")
        private String nodeType;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineageNodeIdRequest {
        @JsonProperty("nodeId")
        private Long nodeId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineageRelationPageRequest extends PageRequest {
        @JsonProperty("versionId")
        private Long versionId;

        @JsonProperty("keyword")
        private String keyword;

        @JsonProperty("relationType")
        private String relationType;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineageRelationIdRequest {
        @JsonProperty("relationId")
        private Long relationId;
    }
}
