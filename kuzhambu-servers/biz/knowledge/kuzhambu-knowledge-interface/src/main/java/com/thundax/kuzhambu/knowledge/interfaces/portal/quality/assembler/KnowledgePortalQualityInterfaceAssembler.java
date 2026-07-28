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
        KnowledgePortalQualityResponse response = new KnowledgePortalQualityResponse();
        response.setQualityStats(toQualityStats(result.getQualityStats()));
        response.setTrendSeries(toTrendSeries(result.getTrendSeries()));
        response.setSourceBreakdowns(toSourceBreakdowns(result.getSourceBreakdowns()));
        response.setFocusIssues(toFocusIssues(result.getFocusIssues()));
        response.setSourceDetails(toSourceDetails(result.getSourceDetails()));
        return response;
    }

    private static List<KnowledgePortalQualityResponse.QualityStatResponse> toQualityStats(
            List<KnowledgePortalQualityResult.QualityStatItem> qualityStats) {
        if (qualityStats == null || qualityStats.isEmpty()) {
            return Collections.emptyList();
        }
        return qualityStats.stream()
                .map(item -> new KnowledgePortalQualityResponse.QualityStatResponse(
                        item.getKey(),
                        item.getLabel(),
                        item.getValue(),
                        item.getUnit(),
                        item.getDeltaText(),
                        item.getStatusTone()))
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.TrendSeriesResponse> toTrendSeries(
            List<KnowledgePortalQualityResult.TrendSeries> trendSeries) {
        if (trendSeries == null || trendSeries.isEmpty()) {
            return Collections.emptyList();
        }
        return trendSeries.stream()
                .map(series -> new KnowledgePortalQualityResponse.TrendSeriesResponse(
                        series.getSeriesKey(), series.getSeriesLabel(), toTrendPoints(series.getPoints())))
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.TrendPointResponse> toTrendPoints(
            List<KnowledgePortalQualityResult.TrendPoint> points) {
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }
        return points.stream()
                .map(point -> new KnowledgePortalQualityResponse.TrendPointResponse(point.getLabel(), point.getValue()))
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.SourceBreakdownResponse> toSourceBreakdowns(
            List<KnowledgePortalQualityResult.SourceBreakdownItem> sourceBreakdowns) {
        if (sourceBreakdowns == null || sourceBreakdowns.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceBreakdowns.stream()
                .map(item -> new KnowledgePortalQualityResponse.SourceBreakdownResponse(
                        item.getSourceKey(), item.getSourceLabel(), item.getValue(), item.getDescription()))
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.FocusIssueResponse> toFocusIssues(
            List<KnowledgePortalQualityResult.FocusIssueItem> focusIssues) {
        if (focusIssues == null || focusIssues.isEmpty()) {
            return Collections.emptyList();
        }
        return focusIssues.stream()
                .map(item -> new KnowledgePortalQualityResponse.FocusIssueResponse(
                        item.getTitle(), item.getSummary(), item.getSeverity(), item.getHref()))
                .toList();
    }

    private static List<KnowledgePortalQualityResponse.SourceDetailResponse> toSourceDetails(
            List<KnowledgePortalQualityResult.SourceDetailItem> sourceDetails) {
        if (sourceDetails == null || sourceDetails.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceDetails.stream()
                .map(item -> new KnowledgePortalQualityResponse.SourceDetailResponse(
                        item.getSourceType(),
                        item.getSourceTitle(),
                        item.getUpdatedAt(),
                        item.getStatus(),
                        item.getHref()))
                .toList();
    }
}
