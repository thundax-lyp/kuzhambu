package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "DiscoverySearchAnalysisSummaryResponse", description = "Discovery 搜索分析摘要响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchAnalysisSummaryResponse implements Serializable {

    @Schema(name = "searchCount", description = "搜索次数")
    @JsonProperty(value = "searchCount")
    private Long searchCount;

    @Schema(name = "failedSearchCount", description = "失败搜索次数")
    @JsonProperty(value = "failedSearchCount")
    private Long failedSearchCount;

    @Schema(name = "zeroResultSearchCount", description = "零结果搜索次数")
    @JsonProperty(value = "zeroResultSearchCount")
    private Long zeroResultSearchCount;

    @Schema(name = "clickCount", description = "点击次数")
    @JsonProperty(value = "clickCount")
    private Long clickCount;

    @Schema(name = "topQueries", description = "热门搜索词")
    @JsonProperty(value = "topQueries")
    private List<TopQueryResponse> topQueries;

    @Getter
    @Builder
    @Schema(name = "DiscoverySearchAnalysisSummaryTopQueryResponse", description = "Discovery 热门搜索词响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopQueryResponse implements Serializable {

        @Schema(name = "queryText", description = "搜索词")
        @JsonProperty(value = "queryText")
        private String queryText;

        @Schema(name = "count", description = "搜索次数")
        @JsonProperty(value = "count")
        private Long count;
    }
}
