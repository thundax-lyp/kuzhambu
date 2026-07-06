package com.thundax.kuzhambu.operations.interfaces.admin.restore.assembler;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestoreDetailQuery;
import com.thundax.kuzhambu.operations.application.restore.query.OperationsRestorePageQuery;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreDetailResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestoreExecuteResult;
import com.thundax.kuzhambu.operations.application.restore.result.OperationsRestorePageResult;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestoreExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request.OperationsRestorePageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestoreDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestoreExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response.OperationsRestorePageResponse;

public final class OperationsRestoreInterfaceAssembler {

    private OperationsRestoreInterfaceAssembler() {}

    public static OperationsRestoreExecuteCommand toCommand(OperationsRestoreExecuteRequest request) {
        if (request == null || request.getBackupId() == null) {
            return null;
        }
        return new OperationsRestoreExecuteCommand(
                BackupId.of(request.getBackupId()), request.getRestoreMode(), currentAdminUserId());
    }

    public static OperationsRestorePageQuery toQuery(OperationsRestorePageRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsRestorePageQuery(
                request.getBackupId(),
                request.getRestoreMode(),
                request.getRestoreStatus(),
                request.getRequesterUserId());
    }

    public static OperationsRestoreDetailQuery toQuery(OperationsRestoreDetailRequest request) {
        if (request == null || request.getRestoreId() == null) {
            return null;
        }
        return new OperationsRestoreDetailQuery(RestoreId.of(request.getRestoreId()));
    }

    public static OperationsRestoreExecuteResponse toResponse(OperationsRestoreExecuteResult result) {
        if (result == null) {
            return null;
        }
        return OperationsRestoreExecuteResponse.builder()
                .restoreId(
                        result.getRestoreId() == null
                                ? null
                                : result.getRestoreId().value())
                .backupId(result.getBackupId())
                .preRestoreBackupId(result.getPreRestoreBackupId())
                .restoreStatus(result.getRestoreStatus())
                .writeBlockEnabled(result.getWriteBlockEnabled())
                .failureReason(result.getFailureReason())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    public static OperationsRestorePageResponse toResponse(OperationsRestorePageResult result) {
        if (result == null) {
            return null;
        }
        return OperationsRestorePageResponse.builder()
                .restoreId(
                        result.getRestoreId() == null
                                ? null
                                : result.getRestoreId().value())
                .backupId(result.getBackupId())
                .preRestoreBackupId(result.getPreRestoreBackupId())
                .restoreStatus(result.getRestoreStatus())
                .writeBlockEnabled(result.getWriteBlockEnabled())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    public static OperationsRestoreDetailResponse toDetailResponse(OperationsRestoreDetailResult result) {
        if (result == null) {
            return null;
        }
        return OperationsRestoreDetailResponse.builder()
                .restoreId(
                        result.getRestoreId() == null
                                ? null
                                : result.getRestoreId().value())
                .backupId(result.getBackupId())
                .preRestoreBackupId(result.getPreRestoreBackupId())
                .restoreStatus(result.getRestoreStatus())
                .writeBlockEnabled(result.getWriteBlockEnabled())
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
