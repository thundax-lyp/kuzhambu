package com.thundax.kuzhambu.operations.interfaces.admin.backup.assembler;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupDetailQuery;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupQuery;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupDetailResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupExecuteResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupPageResult;
import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request.OperationsBackupPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response.OperationsBackupPageResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class OperationsBackupInterfaceAssembler {

    private OperationsBackupInterfaceAssembler() {}

    @NonNull
    public static OperationsBackupExecuteCommand toCommand(@NonNull OperationsBackupExecuteRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsBackupExecuteCommand(currentAdminUserId());
    }

    @NonNull
    public static OperationsBackupQuery toQuery(@NonNull OperationsBackupPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsBackupQuery(
                request.getBackupType(), request.getBackupStatus(), request.getRequesterUserId());
    }

    @NonNull
    public static OperationsBackupDetailQuery toQuery(@NonNull OperationsBackupDetailRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsBackupDetailQuery(BackupIdCodec.toDomain(request.getBackupId()));
    }

    @NonNull
    public static OperationsBackupExecuteResponse toResponse(@NonNull OperationsBackupExecuteResult result) {
        Objects.requireNonNull(result, "result must not be null");
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

    @NonNull
    public static OperationsBackupPageResponse toResponse(@NonNull OperationsBackupPageResult result) {
        Objects.requireNonNull(result, "result must not be null");
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

    @NonNull
    public static OperationsBackupDetailResponse toDetailResponse(@NonNull OperationsBackupDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
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
