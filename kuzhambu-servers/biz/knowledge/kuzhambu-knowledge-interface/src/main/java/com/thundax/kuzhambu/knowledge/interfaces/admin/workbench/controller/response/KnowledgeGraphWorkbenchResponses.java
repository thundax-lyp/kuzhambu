package com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public final class KnowledgeGraphWorkbenchResponses {

    private KnowledgeGraphWorkbenchResponses() {}

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptTreeNodeResponse implements Serializable {
        @JsonProperty("nodeKey")
        private String nodeKey;

        @JsonProperty("parentKey")
        private String parentKey;

        @JsonProperty("nodeType")
        private String nodeType;

        @JsonProperty("title")
        private String title;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("sourcePath")
        private String sourcePath;

        @JsonProperty("graphStatus")
        private String graphStatus;

        @JsonProperty("latestTaskId")
        private Long latestTaskId;

        @JsonProperty("latestGraphVersionId")
        private Long latestGraphVersionId;

        @JsonProperty("children")
        private List<ManuscriptTreeNodeResponse> children;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptDetailResponse implements Serializable {
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("summary")
        private String summary;

        @JsonProperty("sourcePath")
        private String sourcePath;

        @JsonProperty("currentVersionNo")
        private Integer currentVersionNo;

        @JsonProperty("graphStatus")
        private String graphStatus;

        @JsonProperty("latestExtractionTask")
        private GraphExtractionResponses.TaskResponse latestExtractionTask;

        @JsonProperty("latestGraphVersion")
        private GraphExtractionResponses.VersionResponse latestGraphVersion;

        @JsonProperty("qualitySummary")
        private QualitySummaryResponse qualitySummary;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateSummaryResponse implements Serializable {
        @JsonProperty("taskId")
        private Long taskId;

        @JsonProperty("aiCandidateId")
        private Long aiCandidateId;

        @JsonProperty("taskType")
        private String taskType;

        @JsonProperty("status")
        private String status;

        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @JsonProperty("candidatePayloadJson")
        private String candidatePayloadJson;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateApplyResponse implements Serializable {
        @JsonProperty("taskId")
        private Long taskId;

        @JsonProperty("graphVersionId")
        private Long graphVersionId;

        @JsonProperty("graphStatus")
        private String graphStatus;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QualitySummaryResponse implements Serializable {
        @JsonProperty("entityCoverageRate")
        private Double entityCoverageRate;

        @JsonProperty("relationAccuracyRate")
        private Double relationAccuracyRate;

        @JsonProperty("completenessRate")
        private Double completenessRate;
    }
}
