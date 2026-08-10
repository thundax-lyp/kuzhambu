package com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "KnowledgePortalQualityResponse", description = "质量门户响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgePortalQualityResponse {
    private List<QualityStatResponse> qualityStats;
    private List<TrendSeriesResponse> trendSeries;
    private List<SourceBreakdownResponse> sourceBreakdowns;
    private List<FocusIssueResponse> focusIssues;
    private List<SourceDetailResponse> sourceDetails;

    @Getter
    @Builder
    @Schema(name = "QualityStatResponse", description = "质量指标")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QualityStatResponse {
        private String key;
        private String label;
        private String value;
        private String unit;
        private String deltaText;
        private String statusTone;
    }

    @Getter
    @Builder
    @Schema(name = "TrendSeriesResponse", description = "趋势序列")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrendSeriesResponse {
        private String seriesKey;
        private String seriesLabel;
        private List<TrendPointResponse> points;
    }

    @Getter
    @Builder
    @Schema(name = "TrendPointResponse", description = "趋势数据点")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrendPointResponse {
        private String label;
        private Long value;
    }

    @Getter
    @Builder
    @Schema(name = "SourceBreakdownResponse", description = "来源明细")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceBreakdownResponse {
        private String sourceKey;
        private String sourceLabel;
        private Long value;
        private String description;
    }

    @Getter
    @Builder
    @Schema(name = "FocusIssueResponse", description = "重点问题")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FocusIssueResponse {
        private String title;
        private String summary;
        private String severity;
        private String href;
    }

    @Getter
    @Builder
    @Schema(name = "SourceDetailResponse", description = "来源详情")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceDetailResponse {
        private String sourceType;
        private String sourceTitle;
        private Long updatedAt;
        private String status;
        private String href;
    }
}
