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

public final class OperationsCleanupInterfaceAssembler {

    private OperationsCleanupInterfaceAssembler() {}

    public static OperationsCleanupExecuteCommand toCommand(OperationsCleanupExecuteRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsCleanupExecuteCommand(request.getCleanupType(), currentAdminUserId());
    }

    public static OperationsCleanupQuery toQuery(OperationsCleanupPageRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsCleanupQuery(
                request.getCleanupType(), request.getCleanupStatus(), request.getRequesterUserId());
    }

    public static OperationsCleanupDetailQuery toQuery(OperationsCleanupDetailRequest request) {
        if (request == null || request.getCleanupId() == null) {
            return null;
        }
        return new OperationsCleanupDetailQuery(CleanupJobIdCodec.toDomain(request.getCleanupId()));
    }

    public static OperationsCleanupExecuteResponse toResponse(OperationsCleanupDetailResult result) {
        if (result == null) {
            return null;
        }
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

    public static OperationsCleanupPageResponse toResponse(OperationsCleanupPageResult result) {
        if (result == null) {
            return null;
        }
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

    public static OperationsCleanupDetailResponse toDetailResponse(OperationsCleanupDetailResult result) {
        if (result == null) {
            return null;
        }
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
