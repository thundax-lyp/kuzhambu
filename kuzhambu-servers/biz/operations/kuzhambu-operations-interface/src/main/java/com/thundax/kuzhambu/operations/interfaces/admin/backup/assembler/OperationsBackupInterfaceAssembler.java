package com.thundax.kuzhambu.operations.interfaces.admin.backup.assembler;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupDetailQuery;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupPageQuery;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupDetailResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupExecuteResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupPageResult;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupPageResponse;

public final class OperationsBackupInterfaceAssembler {

    private OperationsBackupInterfaceAssembler() {}

    public static OperationsBackupExecuteCommand toCommand(OperationsBackupExecuteRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsBackupExecuteCommand(currentAdminUserId());
    }

    public static OperationsBackupPageQuery toQuery(OperationsBackupPageRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsBackupPageQuery(
                request.getBackupType(), request.getBackupStatus(), request.getRequesterUserId());
    }

    public static OperationsBackupDetailQuery toQuery(OperationsBackupDetailRequest request) {
        if (request == null || request.getBackupId() == null) {
            return null;
        }
        return new OperationsBackupDetailQuery(BackupId.of(request.getBackupId()));
    }

    public static OperationsBackupExecuteResponse toResponse(OperationsBackupExecuteResult result) {
        if (result == null) {
            return null;
        }
        return OperationsBackupExecuteResponse.builder()
                .backupId(
                        result.getBackupId() == null
                                ? null
                                : result.getBackupId().value())
                .backupType(result.getBackupType())
                .backupStatus(result.getBackupStatus())
                .fileName(result.getFileName())
                .fileSizeBytes(result.getFileSizeBytes())
                .checksum(result.getChecksum())
                .failureReason(result.getFailureReason())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .expiresAt(result.getExpiresAt())
                .build();
    }

    public static OperationsBackupPageResponse toResponse(OperationsBackupPageResult result) {
        if (result == null) {
            return null;
        }
        return OperationsBackupPageResponse.builder()
                .backupId(
                        result.getBackupId() == null
                                ? null
                                : result.getBackupId().value())
                .backupType(result.getBackupType())
                .backupStatus(result.getBackupStatus())
                .fileName(result.getFileName())
                .fileSizeBytes(result.getFileSizeBytes())
                .checksum(result.getChecksum())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .expiresAt(result.getExpiresAt())
                .build();
    }

    public static OperationsBackupDetailResponse toDetailResponse(OperationsBackupDetailResult result) {
        if (result == null) {
            return null;
        }
        return OperationsBackupDetailResponse.builder()
                .backupId(
                        result.getBackupId() == null
                                ? null
                                : result.getBackupId().value())
                .backupType(result.getBackupType())
                .backupStatus(result.getBackupStatus())
                .storageObjectId(result.getStorageObjectId())
                .fileName(result.getFileName())
                .fileSizeBytes(result.getFileSizeBytes())
                .checksum(result.getChecksum())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .expiresAt(result.getExpiresAt())
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
