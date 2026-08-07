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

        @Schema(name = "entities", description = "候选实体")
        @JsonProperty("entities")
        private List<CandidateEntityResponse> entities;

        @Schema(name = "relations", description = "候选关系")
        @JsonProperty("relations")
        private List<CandidateRelationResponse> relations;

        @Schema(name = "warnings", description = "候选警告")
        @JsonProperty("warnings")
        private List<String> warnings;
    }

    @Getter
    @Builder
    @Schema(name = "KnowledgeGraphWorkbenchCandidateEntityResponse", description = "知识图谱候选实体响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateEntityResponse implements Serializable {
        @Schema(name = "name", description = "实体名称")
        @JsonProperty("name")
        private String name;

        @Schema(name = "entityType", description = "实体类型")
        @JsonProperty("entityType")
        private String entityType;

        @Schema(name = "description", description = "实体说明")
        @JsonProperty("description")
        private String description;
    }

    @Getter
    @Builder
    @Schema(name = "KnowledgeGraphWorkbenchCandidateRelationResponse", description = "知识图谱候选关系响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateRelationResponse implements Serializable {
        @Schema(name = "sourceName", description = "来源实体名称")
        @JsonProperty("sourceName")
        private String sourceName;

        @Schema(name = "sourceType", description = "来源实体类型")
        @JsonProperty("sourceType")
        private String sourceType;

        @Schema(name = "relationType", description = "关系类型")
        @JsonProperty("relationType")
        private String relationType;

        @Schema(name = "targetName", description = "目标实体名称")
        @JsonProperty("targetName")
        private String targetName;

        @Schema(name = "targetType", description = "目标实体类型")
        @JsonProperty("targetType")
        private String targetType;

        @Schema(name = "evidence", description = "证据")
        @JsonProperty("evidence")
        private String evidence;
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
