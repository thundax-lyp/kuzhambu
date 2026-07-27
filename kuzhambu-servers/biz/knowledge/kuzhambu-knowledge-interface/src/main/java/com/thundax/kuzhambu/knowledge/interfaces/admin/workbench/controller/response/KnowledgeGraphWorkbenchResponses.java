package com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public final class KnowledgeGraphWorkbenchResponses {

    private KnowledgeGraphWorkbenchResponses() {}

    @Getter
    @Builder
    @Schema(name = "KnowledgeGraphWorkbenchManuscriptTreeNodeResponse", description = "知识图谱工作台稿件树节点响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptTreeNodeResponse implements Serializable {
        @Schema(name = "nodeKey", description = "树节点键")
        @JsonProperty("nodeKey")
        private String nodeKey;

        @Schema(name = "parentKey", description = "父级树节点键")
        @JsonProperty("parentKey")
        private String parentKey;

        @Schema(name = "nodeType", description = "树节点类型")
        @JsonProperty("nodeType")
        private String nodeType;

        @Schema(name = "title", description = "节点标题")
        @JsonProperty("title")
        private String title;

        @Schema(name = "sourceContentType", description = "来源内容类型")
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @Schema(name = "sourceContentId", description = "来源内容ID")
        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @Schema(name = "sourcePath", description = "来源路径")
        @JsonProperty("sourcePath")
        private String sourcePath;

        @Schema(name = "graphStatus", description = "图谱处理状态")
        @JsonProperty("graphStatus")
        private String graphStatus;

        @Schema(name = "latestTaskId", description = "最近抽取任务ID")
        @JsonProperty("latestTaskId")
        private Long latestTaskId;

        @Schema(name = "latestGraphVersionId", description = "最新图谱版本ID")
        @JsonProperty("latestGraphVersionId")
        private Long latestGraphVersionId;

        @Schema(name = "children", description = "子节点列表")
        @JsonProperty("children")
        private List<ManuscriptTreeNodeResponse> children;
    }

    @Getter
    @Builder
    @Schema(name = "KnowledgeGraphWorkbenchManuscriptDetailResponse", description = "知识图谱工作台稿件详情响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptDetailResponse implements Serializable {
        @Schema(name = "sourceContentType", description = "来源内容类型")
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @Schema(name = "sourceContentId", description = "来源内容ID")
        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @Schema(name = "title", description = "稿件标题")
        @JsonProperty("title")
        private String title;

        @Schema(name = "summary", description = "稿件摘要")
        @JsonProperty("summary")
        private String summary;

        @Schema(name = "sourcePath", description = "来源路径")
        @JsonProperty("sourcePath")
        private String sourcePath;

        @Schema(name = "currentVersionNo", description = "当前内容版本号")
        @JsonProperty("currentVersionNo")
        private Integer currentVersionNo;

        @Schema(name = "graphStatus", description = "图谱处理状态")
        @JsonProperty("graphStatus")
        private String graphStatus;

        @Schema(name = "latestExtractionTask", description = "最近抽取任务")
        @JsonProperty("latestExtractionTask")
        private GraphExtractionResponses.TaskResponse latestExtractionTask;

        @Schema(name = "latestGraphVersion", description = "最新图谱版本")
        @JsonProperty("latestGraphVersion")
        private GraphExtractionResponses.VersionResponse latestGraphVersion;

        @Schema(name = "qualitySummary", description = "质量摘要")
        @JsonProperty("qualitySummary")
        private QualitySummaryResponse qualitySummary;
    }

    @Getter
    @Builder
    @Schema(name = "KnowledgeGraphWorkbenchCandidateSummaryResponse", description = "知识图谱工作台候选摘要响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateSummaryResponse implements Serializable {
        @Schema(name = "taskId", description = "抽取任务ID")
        @JsonProperty("taskId")
        private Long taskId;

        @Schema(name = "aiCandidateId", description = "AI候选结果ID")
        @JsonProperty("aiCandidateId")
        private Long aiCandidateId;

        @Schema(name = "taskType", description = "抽取任务类型")
        @JsonProperty("taskType")
        private String taskType;

        @Schema(name = "status", description = "任务状态")
        @JsonProperty("status")
        private String status;

        @Schema(name = "sourceContentType", description = "来源内容类型")
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @Schema(name = "sourceContentId", description = "来源内容ID")
        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @Schema(name = "candidatePayloadJson", description = "候选结果JSON")
        @JsonProperty("candidatePayloadJson")
        private String candidatePayloadJson;
    }

    @Getter
    @Builder
    @Schema(name = "KnowledgeGraphWorkbenchCandidateApplyResponse", description = "知识图谱工作台候选应用响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateApplyResponse implements Serializable {
        @Schema(name = "taskId", description = "抽取任务ID")
        @JsonProperty("taskId")
        private Long taskId;

        @Schema(name = "graphVersionId", description = "图谱版本ID")
        @JsonProperty("graphVersionId")
        private Long graphVersionId;

        @Schema(name = "graphStatus", description = "图谱处理状态")
        @JsonProperty("graphStatus")
        private String graphStatus;
    }

    @Getter
    @Builder
    @Schema(name = "KnowledgeGraphWorkbenchQualitySummaryResponse", description = "知识图谱工作台质量摘要响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QualitySummaryResponse implements Serializable {
        @Schema(name = "entityCoverageRate", description = "实体覆盖率")
        @JsonProperty("entityCoverageRate")
        private Double entityCoverageRate;

        @Schema(name = "relationAccuracyRate", description = "关系准确率")
        @JsonProperty("relationAccuracyRate")
        private Double relationAccuracyRate;

        @Schema(name = "completenessRate", description = "完整率")
        @JsonProperty("completenessRate")
        private Double completenessRate;
    }
}
