package com.thundax.kuzhambu.operations.interfaces.admin.report.assembler;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.operations.application.report.command.OperationsReportGenerateCommand;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportDetailQuery;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportPageQuery;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDetailResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportGenerateResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportPageResult;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportGenerateRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportGenerateResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportPageResponse;

public final class OperationsReportInterfaceAssembler {

    private OperationsReportInterfaceAssembler() {}

    public static OperationsReportGenerateCommand toCommand(OperationsReportGenerateRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsReportGenerateCommand(
                request.getReportType(),
                request.getFormat(),
                request.getPeriodStart(),
                request.getPeriodEnd(),
                currentAdminUserId());
    }

    public static OperationsReportPageQuery toQuery(OperationsReportPageRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsReportPageQuery(
                request.getReportType(),
                request.getFormat(),
                request.getReportStatus(),
                request.getRequesterUserId(),
                request.getPeriodStart(),
                request.getPeriodEnd());
    }

    public static OperationsReportDetailQuery toQuery(OperationsReportDetailRequest request) {
        if (request == null || request.getReportId() == null) {
            return null;
        }
        return new OperationsReportDetailQuery(ReportId.of(request.getReportId()));
    }

    public static OperationsReportGenerateResponse toResponse(OperationsReportGenerateResult result) {
        if (result == null) {
            return null;
        }
        return OperationsReportGenerateResponse.builder()
                .reportId(
                        result.getReportId() == null
                                ? null
                                : result.getReportId().value())
                .reportStatus(result.getReportStatus())
                .build();
    }

    public static OperationsReportPageResponse toResponse(OperationsReportPageResult result) {
        if (result == null) {
            return null;
        }
        return OperationsReportPageResponse.builder()
                .reportId(
                        result.getReportId() == null
                                ? null
                                : result.getReportId().value())
                .reportType(result.getReportType())
                .format(result.getFormat())
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .storageObjectId(result.getStorageObjectId())
                .artifactFilename(result.getArtifactFilename())
                .reportStatus(result.getReportStatus())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .requestedAt(result.getRequestedAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    public static OperationsReportDetailResponse toDetailResponse(OperationsReportDetailResult result) {
        if (result == null) {
            return null;
        }
        return OperationsReportDetailResponse.builder()
                .reportId(
                        result.getReportId() == null
                                ? null
                                : result.getReportId().value())
                .reportType(result.getReportType())
                .format(result.getFormat())
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .requestId(result.getRequestId())
                .traceId(result.getTraceId())
                .templateVersion(result.getTemplateVersion())
                .storageObjectId(result.getStorageObjectId())
                .artifactFilename(result.getArtifactFilename())
                .reportStatus(result.getReportStatus())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .requestedAt(result.getRequestedAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    private static Long currentAdminUserId() {
        if (KuzhambuContextHolder.currentSubjectType() != KuzhambuSubjectType.ADMIN_USER) {
            return null;
        }
        String subjectId = KuzhambuContextHolder.currentSubjectId();
        if (subjectId == null || subjectId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(subjectId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
