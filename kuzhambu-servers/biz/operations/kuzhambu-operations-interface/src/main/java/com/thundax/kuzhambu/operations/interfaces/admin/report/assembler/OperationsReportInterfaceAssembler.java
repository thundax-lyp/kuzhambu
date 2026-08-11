package com.thundax.kuzhambu.operations.interfaces.admin.report.assembler;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.operations.application.report.command.OperationsReportGenerateCommand;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportDetailQuery;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportQuery;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDetailResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportGenerateResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportPageResult;
import com.thundax.kuzhambu.operations.domain.report.codec.ReportIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportGenerateRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportGenerateResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportPageResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class OperationsReportInterfaceAssembler {

    private OperationsReportInterfaceAssembler() {}

    @NonNull
    public static OperationsReportGenerateCommand toCommand(@NonNull OperationsReportGenerateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsReportGenerateCommand(
                request.getReportType(),
                request.getFormat(),
                request.getPeriodStart(),
                request.getPeriodEnd(),
                currentAdminUserId());
    }

    @NonNull
    public static OperationsReportQuery toQuery(@NonNull OperationsReportPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsReportQuery(
                request.getReportType(),
                request.getFormat(),
                request.getReportStatus(),
                request.getRequesterUserId(),
                request.getPeriodStart(),
                request.getPeriodEnd());
    }

    @NonNull
    public static OperationsReportDetailQuery toQuery(@NonNull OperationsReportDetailRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsReportDetailQuery(ReportIdCodec.toDomain(request.getReportId()));
    }

    @NonNull
    public static OperationsReportDetailQuery toQuery(@NonNull Long reportId) {
        Objects.requireNonNull(reportId, "reportId must not be null");
        return new OperationsReportDetailQuery(ReportIdCodec.toDomain(reportId));
    }

    @NonNull
    public static OperationsReportGenerateResponse toResponse(@NonNull OperationsReportGenerateResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsReportGenerateResponse.builder()
                .reportId(
                        result.getReportId() == null
                                ? null
                                : result.getReportId().value())
                .reportStatus(result.getReportStatus())
                .build();
    }

    @NonNull
    public static OperationsReportPageResponse toResponse(@NonNull OperationsReportPageResult result) {
        Objects.requireNonNull(result, "result must not be null");
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

    @NonNull
    public static OperationsReportDetailResponse toDetailResponse(@NonNull OperationsReportDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
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
