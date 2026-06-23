package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagGovernanceMetricsResponse", description = "标签治理统计响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagGovernanceMetricsResponse implements Serializable {

    @Schema(name = "topTags", description = "标签使用排行")
    @JsonProperty(value = "topTags")
    private List<TagUsageMetric> topTags;

    @Schema(name = "categoryDistributions", description = "知识库分布")
    @JsonProperty(value = "categoryDistributions")
    private List<CategoryDistributionMetric> categoryDistributions;

    @Schema(name = "sourceRatios", description = "来源占比")
    @JsonProperty(value = "sourceRatios")
    private List<SourceRatioMetric> sourceRatios;

    @Schema(name = "monthlyNewTags", description = "月度新增趋势")
    @JsonProperty(value = "monthlyNewTags")
    private List<MonthlyNewTagMetric> monthlyNewTags;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TagUsageMetric implements Serializable {

        @Schema(name = "tagName", description = "标签名称")
        @JsonProperty(value = "tagName")
        private String tagName;

        @Schema(name = "contentRefCount", description = "内容引用数量")
        @JsonProperty(value = "contentRefCount")
        private Long contentRefCount;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryDistributionMetric implements Serializable {

        @Schema(name = "categoryName", description = "分类名称")
        @JsonProperty(value = "categoryName")
        private String categoryName;

        @Schema(name = "tagCount", description = "标签数量")
        @JsonProperty(value = "tagCount")
        private Long tagCount;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceRatioMetric implements Serializable {

        @Schema(name = "source", description = "标签来源")
        @JsonProperty(value = "source")
        private String source;

        @Schema(name = "tagCount", description = "标签数量")
        @JsonProperty(value = "tagCount")
        private Long tagCount;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MonthlyNewTagMetric implements Serializable {

        @Schema(name = "month", description = "月份")
        @JsonProperty(value = "month")
        private String month;

        @Schema(name = "tagCount", description = "标签数量")
        @JsonProperty(value = "tagCount")
        private Long tagCount;
    }
}
