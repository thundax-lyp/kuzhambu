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
}
