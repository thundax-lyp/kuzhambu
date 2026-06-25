package com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePortalQualityResponse {
    private List<QualityStatResponse> qualityStats;
    private List<TrendSeriesResponse> trendSeries;
    private List<SourceBreakdownResponse> sourceBreakdowns;
    private List<FocusIssueResponse> focusIssues;
    private List<SourceDetailResponse> sourceDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityStatResponse {
        private String key;
        private String label;
        private String value;
        private String unit;
        private String deltaText;
        private String statusTone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendSeriesResponse {
        private String seriesKey;
        private String seriesLabel;
        private List<TrendPointResponse> points;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPointResponse {
        private String label;
        private Long value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceBreakdownResponse {
        private String sourceKey;
        private String sourceLabel;
        private Long value;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FocusIssueResponse {
        private String title;
        private String summary;
        private String severity;
        private String href;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDetailResponse {
        private String sourceType;
        private String sourceTitle;
        private Long updatedAt;
        private String status;
        private String href;
    }
}
