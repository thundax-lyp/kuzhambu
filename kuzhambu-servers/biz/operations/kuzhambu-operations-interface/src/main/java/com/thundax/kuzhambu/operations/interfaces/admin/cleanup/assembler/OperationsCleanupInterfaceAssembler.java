package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.assembler;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupDetailQuery;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupQuery;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupPageResult;
import com.thundax.kuzhambu.operations.domain.cleanup.codec.CleanupJobIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupExecuteRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request.OperationsCleanupPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response.OperationsCleanupDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response.OperationsCleanupExecuteResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response.OperationsCleanupPageResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class OperationsCleanupInterfaceAssembler {

    private OperationsCleanupInterfaceAssembler() {}

    @NonNull
    public static OperationsCleanupExecuteCommand toCommand(@NonNull OperationsCleanupExecuteRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsCleanupExecuteCommand(request.getCleanupType(), currentAdminUserId());
    }

    @NonNull
    public static OperationsCleanupQuery toQuery(@NonNull OperationsCleanupPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsCleanupQuery(
                request.getCleanupType(), request.getCleanupStatus(), request.getRequesterUserId());
    }

    @NonNull
    public static OperationsCleanupDetailQuery toQuery(@NonNull OperationsCleanupDetailRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsCleanupDetailQuery(CleanupJobIdCodec.toDomain(request.getCleanupId()));
    }

    @NonNull
    public static OperationsCleanupExecuteResponse toResponse(@NonNull OperationsCleanupDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsCleanupExecuteResponse.builder()
                .cleanupId(
                        result.getCleanupId() == null
                                ? null
                                : result.getCleanupId().value())
                .cleanupType(result.getCleanupType())
                .cleanupStatus(result.getCleanupStatus())
                .totalCount(result.getTotalCount())
                .successCount(result.getSuccessCount())
                .failedCount(result.getFailedCount())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    @NonNull
    public static OperationsCleanupPageResponse toResponse(@NonNull OperationsCleanupPageResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsCleanupPageResponse.builder()
                .cleanupId(
                        result.getCleanupId() == null
                                ? null
                                : result.getCleanupId().value())
                .cleanupType(result.getCleanupType())
                .cleanupStatus(result.getCleanupStatus())
                .totalCount(result.getTotalCount())
                .successCount(result.getSuccessCount())
                .failedCount(result.getFailedCount())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .build();
    }

    @NonNull
    public static OperationsCleanupDetailResponse toDetailResponse(@NonNull OperationsCleanupDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsCleanupDetailResponse.builder()
                .cleanupId(
                        result.getCleanupId() == null
                                ? null
                                : result.getCleanupId().value())
                .cleanupType(result.getCleanupType())
                .cleanupStatus(result.getCleanupStatus())
                .totalCount(result.getTotalCount())
                .successCount(result.getSuccessCount())
                .failedCount(result.getFailedCount())
                .failureReason(result.getFailureReason())
                .requesterUserId(result.getRequesterUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .items(
                        result.getItems() == null
                                ? java.util.List.of()
                                : result.getItems().stream()
                                        .map(item -> OperationsCleanupDetailResponse.Item.builder()
                                                .cleanupItemId(
                                                        item.getCleanupItemId() == null
                                                                ? null
                                                                : item.getCleanupItemId()
                                                                        .value())
                                                .targetType(item.getTargetType())
                                                .targetId(item.getTargetId())
                                                .itemStatus(item.getItemStatus())
                                                .failureReason(item.getFailureReason())
                                                .processedAt(item.getProcessedAt())
                                                .build())
                                        .toList())
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
