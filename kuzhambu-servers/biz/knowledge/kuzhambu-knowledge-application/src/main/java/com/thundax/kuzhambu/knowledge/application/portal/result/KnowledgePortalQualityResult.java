package com.thundax.kuzhambu.knowledge.application.portal.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePortalQualityResult {
    private List<QualityStatItem> qualityStats;
    private List<TrendSeries> trendSeries;
    private List<SourceBreakdownItem> sourceBreakdowns;
    private List<FocusIssueItem> focusIssues;
    private List<SourceDetailItem> sourceDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityStatItem {
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
    public static class TrendSeries {
        private String seriesKey;
        private String seriesLabel;
        private List<TrendPoint> points;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String label;
        private Long value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceBreakdownItem {
        private String sourceKey;
        private String sourceLabel;
        private Long value;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FocusIssueItem {
        private String title;
        private String summary;
        private String severity;
        private String href;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDetailItem {
        private String sourceType;
        private String sourceTitle;
        private Long updatedAt;
        private String status;
        private String href;
    }
}
