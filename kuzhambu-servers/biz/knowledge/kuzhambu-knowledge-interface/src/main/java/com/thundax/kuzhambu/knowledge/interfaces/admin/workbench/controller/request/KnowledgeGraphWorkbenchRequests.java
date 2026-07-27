package com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public final class KnowledgeGraphWorkbenchRequests {

    private KnowledgeGraphWorkbenchRequests() {}

    @Getter
    @Setter
    @Schema(name = "KnowledgeGraphWorkbenchManuscriptTreeRequest", description = "知识图谱工作台稿件树查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptTreeRequest {
        @Schema(name = "sourceContentType", description = "来源内容类型")
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @Schema(name = "parentKey", description = "父级树节点键")
        @JsonProperty("parentKey")
        private String parentKey;

        @Schema(name = "keyword", description = "稿件标题、摘要或分类搜索关键词")
        @JsonProperty("keyword")
        private String keyword;

        @Schema(name = "graphStatus", description = "图谱处理状态")
        @JsonProperty("graphStatus")
        private String graphStatus;
    }

    @Getter
    @Setter
    @Schema(name = "KnowledgeGraphWorkbenchManuscriptRequest", description = "知识图谱工作台稿件详情请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptRequest {
        @Schema(name = "sourceContentType", description = "来源内容类型")
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @Schema(name = "sourceContentId", description = "来源内容ID")
        @JsonProperty("sourceContentId")
        private Long sourceContentId;
    }

    @Getter
    @Setter
    @Schema(name = "KnowledgeGraphWorkbenchManuscriptExtractRequest", description = "知识图谱工作台稿件抽取请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManuscriptExtractRequest {
        @Schema(name = "sourceContentType", description = "来源内容类型")
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @Schema(name = "sourceContentId", description = "来源内容ID")
        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @Schema(name = "taskType", description = "抽取任务类型")
        @JsonProperty("taskType")
        private String taskType;
    }

    @Getter
    @Setter
    @Schema(name = "KnowledgeGraphWorkbenchCandidateRequest", description = "知识图谱工作台候选结果查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateRequest {
        @Schema(name = "sourceContentType", description = "来源内容类型")
        @JsonProperty("sourceContentType")
        private String sourceContentType;

        @Schema(name = "sourceContentId", description = "来源内容ID")
        @JsonProperty("sourceContentId")
        private Long sourceContentId;

        @Schema(name = "taskType", description = "抽取任务类型")
        @JsonProperty("taskType")
        private String taskType;
    }

    @Getter
    @Setter
    @Schema(name = "KnowledgeGraphWorkbenchCandidateApplyRequest", description = "知识图谱工作台候选结果应用请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CandidateApplyRequest {
        @Schema(name = "taskId", description = "抽取任务ID")
        @JsonProperty("taskId")
        private Long taskId;
    }
}
