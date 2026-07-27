package com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public final class KnowledgeGraphWorkbenchRequests {

    private KnowledgeGraphWorkbenchRequests() {}

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptTreeRequest {
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("parentKey")
        private String parentKey;

        @JsonProperty("keyword")
        private String keyword;

        @JsonProperty("graphStatus")
        private String graphStatus;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptRequest {
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptExtractRequest {
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("requestedBy")
        private Long requestedBy;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateRequest {
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("taskType")
        private String taskType;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateApplyRequest {
        @JsonProperty("taskId")
        private Long taskId;
    }
}
