package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler;

import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ReextractLowQualityCategoryCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.LatestQualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportDetailQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.IssueRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.SourceDetailRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.ReextractLowQualityCategoryResult;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request.QualityReportRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.response.QualityReportResponses;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class KnowledgeQualityReportInterfaceAssembler {

    private KnowledgeQualityReportInterfaceAssembler() {}

    @NonNull
    public static GenerateQualityReportCommand toCommand(@NonNull QualityReportRequests.GenerateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GenerateQualityReportCommand(request.getGraphVersionId(), request.getGeneratedBy());
    }

    @NonNull
    public static ReextractLowQualityCategoryCommand toCommand(
            @NonNull QualityReportRequests.ReextractRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ReextractLowQualityCategoryCommand(
                request.getReportId(),
                request.getSourceCategoryCode(),
                request.getTaskType(),
                request.getReplaceUnconfirmedOnly(),
                request.getModelId(),
                request.getModelName(),
                request.getPromptMessagesJson(),
                request.getInputPayloadJson(),
                request.getRequestedBy());
    }

    @NonNull
    public static QualityReportQuery toQuery(@NonNull QualityReportRequests.PageRequestBody request) {
        Objects.requireNonNull(request, "request must not be null");
        return new QualityReportQuery(
                request.getGraphVersionId(),
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getReportStatus());
    }

    @NonNull
    public static QualityReportDetailQuery toDetailQuery(@NonNull QualityReportRequests.DetailRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new QualityReportDetailQuery(request.getReportId());
    }

    @NonNull
    public static LatestQualityReportQuery toLatestQuery(@NonNull QualityReportRequests.LatestRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new LatestQualityReportQuery(request.getGraphVersionId());
    }

    @NonNull
    public static QualityReportResponses.DetailResponse toResponse(@NonNull QualityReportDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return QualityReportResponses.DetailResponse.builder()
                .report(result.getReport() == null ? null : toResponse(result.getReport()))
                .issues(mapIssues(result.getIssues()))
                .sourceDetails(mapSourceDetails(result.getSourceDetails()))
                .annotations(mapAnnotations(result.getAnnotations()))
                .stale(result.getStale())
                .staleReason(result.getStaleReason())
                .lastRefinementAppliedAt(result.getLastRefinementAppliedAt())
                .build();
    }

    @NonNull
    public static QualityReportResponses.ReportResponse toResponse(@NonNull ReportRecord result) {
        Objects.requireNonNull(result, "result must not be null");
        return QualityReportResponses.ReportResponse.builder()
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

    @NonNull
    public static QualityReportResponses.ReextractResponse toResponse(
            @NonNull ReextractLowQualityCategoryResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return QualityReportResponses.ReextractResponse.builder()
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
                .issueId(result.getIssueId())
                .issueType(result.getIssueType())
                .severity(result.getSeverity())
                .objectType(result.getObjectType())
                .objectKey(result.getObjectKey())
                .title(result.getTitle())
                .description(result.getDescription())
                .suggestion(result.getSuggestion())
                .href(result.getHref())
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
                .detailId(result.getDetailId())
                .sourceContentType(result.getSourceContentType())
                .sourceContentId(result.getSourceContentId())
                .sourceCategoryCode(result.getSourceCategoryCode())
                .sourceCategoryName(result.getSourceCategoryName())
                .graphVersionId(result.getGraphVersionId())
                .appliedAt(result.getAppliedAt())
                .annotationCount(result.getAnnotationCount())
                .issueCount(result.getIssueCount())
                .status(result.getStatus())
                .href(result.getHref())
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
                .annotationId(result.getAnnotationId())
                .objectType(result.getObjectType())
                .objectKey(result.getObjectKey())
                .graphVersionId(result.getGraphVersionId())
                .annotationStatus(result.getAnnotationStatus())
                .annotationLabel(result.getAnnotationLabel())
                .comment(result.getComment())
                .build();
    }
}
