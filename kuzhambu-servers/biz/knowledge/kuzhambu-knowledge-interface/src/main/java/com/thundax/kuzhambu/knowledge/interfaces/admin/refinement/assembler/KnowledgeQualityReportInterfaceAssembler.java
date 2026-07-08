package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler;

import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ReextractLowQualityCategoryCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportPageQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.IssueRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.SourceDetailRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.ReextractLowQualityCategoryResult;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request.QualityReportRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.response.QualityReportResponses;
import java.util.List;

public final class KnowledgeQualityReportInterfaceAssembler {

    private KnowledgeQualityReportInterfaceAssembler() {}

    public static GenerateQualityReportCommand toCommand(QualityReportRequests.GenerateRequest request) {
        return new GenerateQualityReportCommand(
                request == null ? null : request.getGraphVersionId(),
                request == null ? null : request.getGeneratedBy());
    }

    public static ReextractLowQualityCategoryCommand toCommand(QualityReportRequests.ReextractRequest request) {
        return new ReextractLowQualityCategoryCommand(
                request == null ? null : request.getReportId(),
                request == null ? null : request.getSourceCategoryCode(),
                request == null ? null : request.getTaskType(),
                request == null ? null : request.getReplaceUnconfirmedOnly(),
                request == null ? null : request.getModelId(),
                request == null ? null : request.getModelName(),
                request == null ? null : request.getPromptMessagesJson(),
                request == null ? null : request.getInputPayloadJson(),
                request == null ? null : request.getRequestedBy());
    }

    public static QualityReportPageQuery toPageQuery(QualityReportRequests.PageRequestBody request) {
        return new QualityReportPageQuery(
                request == null ? null : request.getGraphVersionId(),
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getReportStatus(),
                request == null || request.getPageNo() == null ? 1 : request.getPageNo(),
                request == null || request.getPageSize() == null ? 20 : request.getPageSize());
    }

    public static QualityReportResponses.DetailResponse toResponse(QualityReportDetailResult result) {
        return QualityReportResponses.DetailResponse.builder()
                .report(toResponse(result == null ? null : result.getReport()))
                .issues(mapIssues(result == null ? null : result.getIssues()))
                .sourceDetails(mapSourceDetails(result == null ? null : result.getSourceDetails()))
                .annotations(mapAnnotations(result == null ? null : result.getAnnotations()))
                .stale(result == null ? null : result.getStale())
                .staleReason(result == null ? null : result.getStaleReason())
                .lastRefinementAppliedAt(result == null ? null : result.getLastRefinementAppliedAt())
                .build();
    }

    public static QualityReportResponses.ReportResponse toResponse(ReportRecord result) {
        return result == null
                ? null
                : QualityReportResponses.ReportResponse.builder()
                        .reportId(result.getReportId())
                        .reportNo(result.getReportNo())
                        .graphVersionId(result.getGraphVersionId())
                        .sourceContentType(result.getSourceContentType())
                        .sourceContentId(result.getSourceContentId())
                        .sourceCategoryCode(result.getSourceCategoryCode())
                        .sourceCategoryName(result.getSourceCategoryName())
                        .reportStatus(result.getReportStatus())
                        .entityTotalCount(result.getEntityTotalCount())
                        .entityConfirmedCount(result.getEntityConfirmedCount())
                        .relationTotalCount(result.getRelationTotalCount())
                        .relationConfirmedCount(result.getRelationConfirmedCount())
                        .lineageTotalCount(result.getLineageTotalCount())
                        .lineageConfirmedCount(result.getLineageConfirmedCount())
                        .entityCoverageRate(result.getEntityCoverageRate())
                        .relationAccuracyRate(result.getRelationAccuracyRate())
                        .lineageCoverageRate(result.getLineageCoverageRate())
                        .completenessRate(result.getCompletenessRate())
                        .annotationCount(result.getAnnotationCount())
                        .issueCount(result.getIssueCount())
                        .generatedBy(result.getGeneratedBy())
                        .generatedAt(result.getGeneratedAt())
                        .publishedAt(result.getPublishedAt())
                        .build();
    }

    public static QualityReportResponses.ReextractResponse toResponse(ReextractLowQualityCategoryResult result) {
        return result == null
                ? null
                : QualityReportResponses.ReextractResponse.builder()
                        .reportId(result.getReportId())
                        .sourceCategoryCode(result.getSourceCategoryCode())
                        .sourceCategoryName(result.getSourceCategoryName())
                        .sourceContentType(result.getSourceContentType())
                        .sourceContentId(result.getSourceContentId())
                        .taskId(result.getTaskId())
                        .batchJobId(result.getBatchJobId())
                        .taskType(result.getTaskType())
                        .triggerSource(result.getTriggerSource())
                        .selectionScopeJson(result.getSelectionScopeJson())
                        .replaceUnconfirmedOnly(result.getReplaceUnconfirmedOnly())
                        .build();
    }

    private static List<QualityReportResponses.IssueResponse> mapIssues(List<IssueRecord> issues) {
        return issues == null
                ? List.of()
                : issues.stream()
                        .map(KnowledgeQualityReportInterfaceAssembler::toResponse)
                        .toList();
    }

    private static QualityReportResponses.IssueResponse toResponse(IssueRecord result) {
        return QualityReportResponses.IssueResponse.builder()
                .issueId(result == null ? null : result.getIssueId())
                .issueType(result == null ? null : result.getIssueType())
                .severity(result == null ? null : result.getSeverity())
                .objectType(result == null ? null : result.getObjectType())
                .objectKey(result == null ? null : result.getObjectKey())
                .title(result == null ? null : result.getTitle())
                .description(result == null ? null : result.getDescription())
                .suggestion(result == null ? null : result.getSuggestion())
                .href(result == null ? null : result.getHref())
                .priority(result == null ? null : result.getPriority())
                .build();
    }

    private static List<QualityReportResponses.SourceDetailResponse> mapSourceDetails(
            List<SourceDetailRecord> sourceDetails) {
        return sourceDetails == null
                ? List.of()
                : sourceDetails.stream()
                        .map(KnowledgeQualityReportInterfaceAssembler::toResponse)
                        .toList();
    }

    private static QualityReportResponses.SourceDetailResponse toResponse(SourceDetailRecord result) {
        return QualityReportResponses.SourceDetailResponse.builder()
                .detailId(result == null ? null : result.getDetailId())
                .sourceContentType(result == null ? null : result.getSourceContentType())
                .sourceContentId(result == null ? null : result.getSourceContentId())
                .sourceCategoryCode(result == null ? null : result.getSourceCategoryCode())
                .sourceCategoryName(result == null ? null : result.getSourceCategoryName())
                .graphVersionId(result == null ? null : result.getGraphVersionId())
                .appliedAt(result == null ? null : result.getAppliedAt())
                .annotationCount(result == null ? null : result.getAnnotationCount())
                .issueCount(result == null ? null : result.getIssueCount())
                .status(result == null ? null : result.getStatus())
                .href(result == null ? null : result.getHref())
                .build();
    }

    private static List<QualityReportResponses.AnnotationResponse> mapAnnotations(
            List<QualityAnnotationResult> annotations) {
        return annotations == null
                ? List.of()
                : annotations.stream()
                        .map(KnowledgeQualityReportInterfaceAssembler::toResponse)
                        .toList();
    }

    private static QualityReportResponses.AnnotationResponse toResponse(QualityAnnotationResult result) {
        return QualityReportResponses.AnnotationResponse.builder()
                .annotationId(result == null ? null : result.getAnnotationId())
                .objectType(result == null ? null : result.getObjectType())
                .objectKey(result == null ? null : result.getObjectKey())
                .graphVersionId(result == null ? null : result.getGraphVersionId())
                .annotationStatus(result == null ? null : result.getAnnotationStatus())
                .annotationLabel(result == null ? null : result.getAnnotationLabel())
                .comment(result == null ? null : result.getComment())
                .build();
    }
}
