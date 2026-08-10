package com.thundax.kuzhambu.knowledge.interfaces.portal.quality.assembler;

import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalQualityResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.response.KnowledgePortalQualityResponse;
import java.util.Collections;
import java.util.List;

public final class KnowledgePortalQualityInterfaceAssembler {

    private KnowledgePortalQualityInterfaceAssembler() {}

    public static KnowledgePortalQualityResponse toResponse(KnowledgePortalQualityResult result) {
        if (result == null) {
            return null;
        }
        return KnowledgePortalQualityResponse.builder()
                .qualityStats(toQualityStats(result.getQualityStats()))
                .trendSeries(toTrendSeries(result.getTrendSeries()))
                .sourceBreakdowns(toSourceBreakdowns(result.getSourceBreakdowns()))
                .focusIssues(toFocusIssues(result.getFocusIssues()))
                .sourceDetails(toSourceDetails(result.getSourceDetails()))
                .build();
    }

    private static List<KnowledgePortalQualityResponse.QualityStatResponse> toQualityStats(
            List<KnowledgePortalQualityResult.QualityStatItem> qualityStats) {
        if (qualityStats == null || qualityStats.isEmpty()) {
            return Collections.emptyList();
        }
        return qualityStats.stream()
                .map(item -> KnowledgePortalQualityResponse.QualityStatResponse.builder()
                        .key(item.getKey())
                        .label(item.getLabel())
                        .value(item.getValue())
                        .unit(item.getUnit())
                        .deltaText(item.getDeltaText())
                        .statusTone(item.getStatusTone())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.TrendSeriesResponse> toTrendSeries(
            List<KnowledgePortalQualityResult.TrendSeries> trendSeries) {
        if (trendSeries == null || trendSeries.isEmpty()) {
            return Collections.emptyList();
        }
        return trendSeries.stream()
                .map(series -> KnowledgePortalQualityResponse.TrendSeriesResponse.builder()
                        .seriesKey(series.getSeriesKey())
                        .seriesLabel(series.getSeriesLabel())
                        .points(toTrendPoints(series.getPoints()))
                        .build())
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.TrendPointResponse> toTrendPoints(
            List<KnowledgePortalQualityResult.TrendPoint> points) {
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }
        return points.stream()
                .map(point -> KnowledgePortalQualityResponse.TrendPointResponse.builder()
                        .label(point.getLabel())
                        .value(point.getValue())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.SourceBreakdownResponse> toSourceBreakdowns(
            List<KnowledgePortalQualityResult.SourceBreakdownItem> sourceBreakdowns) {
        if (sourceBreakdowns == null || sourceBreakdowns.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceBreakdowns.stream()
                .map(item -> KnowledgePortalQualityResponse.SourceBreakdownResponse.builder()
                        .sourceKey(item.getSourceKey())
                        .sourceLabel(item.getSourceLabel())
                        .value(item.getValue())
                        .description(item.getDescription())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.FocusIssueResponse> toFocusIssues(
            List<KnowledgePortalQualityResult.FocusIssueItem> focusIssues) {
        if (focusIssues == null || focusIssues.isEmpty()) {
            return Collections.emptyList();
        }
        return focusIssues.stream()
                .map(item -> KnowledgePortalQualityResponse.FocusIssueResponse.builder()
                        .title(item.getTitle())
                        .summary(item.getSummary())
                        .severity(item.getSeverity())
                        .href(item.getHref())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.SourceDetailResponse> toSourceDetails(
            List<KnowledgePortalQualityResult.SourceDetailItem> sourceDetails) {
        if (sourceDetails == null || sourceDetails.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceDetails.stream()
                .map(item -> KnowledgePortalQualityResponse.SourceDetailResponse.builder()
                        .sourceType(item.getSourceType())
                        .sourceTitle(item.getSourceTitle())
                        .updatedAt(item.getUpdatedAt())
                        .status(item.getStatus())
                        .href(item.getHref())
                        .build())
                .toList();
    }
}
