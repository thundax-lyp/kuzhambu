package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import lombok.Getter;
import lombok.Setter;

public final class QualityReportRequests {

    private QualityReportRequests() {}

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenerateRequest {
        @JsonProperty("graphVersionId")
        private Long graphVersionId;

        @JsonProperty("generatedBy")
        private Long generatedBy;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageRequestBody extends PageRequest {
        @JsonProperty("graphVersionId")
        private Long graphVersionId;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("reportStatus")
        private String reportStatus;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailRequest {
        @JsonProperty("reportId")
        private Long reportId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LatestRequest {
        @JsonProperty("graphVersionId")
        private Long graphVersionId;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReextractRequest {
        @JsonProperty("reportId")
        private Long reportId;

        @JsonProperty("sourceCategoryCode")
        private String sourceCategoryCode;

        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("replaceUnconfirmedOnly")
        private Boolean replaceUnconfirmedOnly;

        @JsonProperty("modelId")
        private Long modelId;

        @JsonProperty("modelName")
        private String modelName;

        @JsonProperty("promptMessagesJson")
        private String promptMessagesJson;

        @JsonProperty("inputPayloadJson")
        private String inputPayloadJson;

        @JsonProperty("requestedBy")
        private Long requestedBy;
    }
}
