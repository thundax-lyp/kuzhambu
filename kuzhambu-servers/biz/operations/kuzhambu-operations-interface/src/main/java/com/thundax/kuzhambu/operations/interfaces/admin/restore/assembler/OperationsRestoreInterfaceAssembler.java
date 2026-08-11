package com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestoreDetailQuery;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestoreQuery;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreDetailResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreExecuteResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestorePageResult;
import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.domain.restore.codec.RestoreIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestorePageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestoreDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestoreExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestorePageResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class OperationsRestoreInterfaceAssembler {

    private OperationsRestoreInterfaceAssembler() {}

    @NonNull
    public static OperationsRestoreExecuteCommand toCommand(@NonNull OperationsRestoreExecuteRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsRestoreExecuteCommand(
                BackupIdCodec.toDomain(request.getBackupId()), request.getRestoreMode(), currentAdminUserId());
    }

    @NonNull
    public static OperationsRestoreQuery toQuery(@NonNull OperationsRestorePageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsRestoreQuery(
                request.getBackupId(),
                request.getRestoreMode(),
                request.getRestoreStatus(),
                request.getRequesterUserId());
    }

    @NonNull
    public static OperationsRestoreDetailQuery toQuery(@NonNull OperationsRestoreDetailRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsRestoreDetailQuery(RestoreIdCodec.toDomain(request.getRestoreId()));
    }

    @NonNull
    public static OperationsRestoreExecuteResponse toResponse(@NonNull OperationsRestoreExecuteResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsRestoreExecuteResponse.builder()
                .restoreId(
                        result.getRestoreId() == null
                                ? null
                                : result.getRestoreId().value())
                .backupId(result.getBackupId())
                .preRestoreBackupId(result.getPreRestoreBackupId())
                .restoreMode(result.getRestoreMode())
                .restoreStatus(result.getRestoreStatus())
                .writeBlockEnabled(result.getWriteBlockEnabled())
                .writeBlockStartedAt(result.getWriteBlockStartedAt())
                .writeBlockReleasedAt(result.getWriteBlockReleasedAt())
                .failureReason(result.getFailureReason())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    @NonNull
    public static OperationsRestorePageResponse toResponse(@NonNull OperationsRestorePageResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsRestorePageResponse.builder()
                .restoreId(
                        result.getRestoreId() == null
                                ? null
                                : result.getRestoreId().value())
                .backupId(result.getBackupId())
                .preRestoreBackupId(result.getPreRestoreBackupId())
                .restoreMode(result.getRestoreMode())
                .restoreStatus(result.getRestoreStatus())
                .writeBlockEnabled(result.getWriteBlockEnabled())
                .writeBlockStartedAt(result.getWriteBlockStartedAt())
                .writeBlockReleasedAt(result.getWriteBlockReleasedAt())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    @NonNull
    public static OperationsRestoreDetailResponse toDetailResponse(@NonNull OperationsRestoreDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsRestoreDetailResponse.builder()
                .restoreId(
                        result.getRestoreId() == null
                                ? null
                                : result.getRestoreId().value())
                .backupId(result.getBackupId())
                .preRestoreBackupId(result.getPreRestoreBackupId())
                .restoreMode(result.getRestoreMode())
                .restoreStatus(result.getRestoreStatus())
                .writeBlockEnabled(result.getWriteBlockEnabled())
                .writeBlockStartedAt(result.getWriteBlockStartedAt())
                .writeBlockReleasedAt(result.getWriteBlockReleasedAt())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .startedAt(result.getStartedAt())
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
