package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class GraphWorkbenchRequests {

    private GraphWorkbenchRequests() {}

    @Getter
    @Setter
    @Schema(description = "图谱工作台概览查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverviewGetRequest {}

    @Getter
    @Setter
    @Schema(description = "图谱工作台最近关系查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecentEdgesListRequest {}

    @Getter
    @Setter
    @Schema(description = "图谱工作台关联边渐进查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IncidentEdgesListRequest {

        @NotEmpty
        private List<@Pattern(regexp = "^\\d+$") String> nodeIds;

        @Pattern(regexp = "^\\d+$")
        private String afterEdgeId;

        @Min(1)
        @Max(200)
        private Integer pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱工作台全局搜索分页请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchPageRequest {

        @Size(max = 128)
        private String keyword;

        @Size(max = 64)
        private String nodeType;

        @Size(max = 64)
        private String relationType;

        @Pattern(regexp = "^\\d+$")
        private String pageNo;

        @Pattern(regexp = "^\\d+$")
        private String pageSize;
    }

    @Getter
    @Setter
    @Schema(description = "图谱工作台质量待办查询请求")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QualityGetRequest {

        @Pattern(regexp = "ISOLATED_NODE|MISSING_CORE_RELATION")
        private String issueType;

        @Size(max = 64)
        private String nodeType;
    }
}
