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
}
