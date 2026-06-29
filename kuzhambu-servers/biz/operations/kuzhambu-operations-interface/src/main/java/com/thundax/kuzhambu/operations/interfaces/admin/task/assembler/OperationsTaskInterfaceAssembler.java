package com.thundax.kuzhambu.operations.interfaces.admin.task.assembler;

import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskDetailQuery;
import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskPageQuery;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskDetailResult;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskPageResult;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.request.OperationsTaskDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.request.OperationsTaskPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response.OperationsTaskDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response.OperationsTaskPageResponse;

public final class OperationsTaskInterfaceAssembler {

    private OperationsTaskInterfaceAssembler() {}

    public static OperationsTaskPageQuery toQuery(OperationsTaskPageRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsTaskPageQuery(request.getSourceDomain(), request.getTaskType(), request.getTaskStatus());
    }

    public static OperationsTaskDetailQuery toQuery(OperationsTaskDetailRequest request) {
        if (request == null || request.getSnapshotId() == null) {
            return null;
        }
        return new OperationsTaskDetailQuery(LongTaskSnapshotId.of(request.getSnapshotId()));
    }

    public static OperationsTaskPageResponse toResponse(OperationsTaskPageResult result) {
        if (result == null) {
            return null;
        }
        return OperationsTaskPageResponse.builder()
                .snapshotId(
                        result.getSnapshotId() == null
                                ? null
                                : result.getSnapshotId().value())
                .sourceDomain(result.getSourceDomain())
                .taskType(result.getTaskType())
                .taskKey(result.getTaskKey())
                .taskStatus(result.getTaskStatus())
                .totalCount(result.getTotalCount())
                .successCount(result.getSuccessCount())
                .failedCount(result.getFailedCount())
                .failureReason(result.getFailureReason())
                .requestedByUserId(result.getRequestedByUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .snapshotAt(result.getSnapshotAt())
                .build();
    }

    public static OperationsTaskDetailResponse toDetailResponse(OperationsTaskDetailResult result) {
        if (result == null) {
            return null;
        }
        return OperationsTaskDetailResponse.builder()
                .snapshotId(
                        result.getSnapshotId() == null
                                ? null
                                : result.getSnapshotId().value())
                .sourceDomain(result.getSourceDomain())
                .taskType(result.getTaskType())
                .taskKey(result.getTaskKey())
                .taskStatus(result.getTaskStatus())
                .totalCount(result.getTotalCount())
                .successCount(result.getSuccessCount())
                .failedCount(result.getFailedCount())
                .failureReason(result.getFailureReason())
                .requestedByUserId(result.getRequestedByUserId())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .snapshotAt(result.getSnapshotAt())
                .build();
    }
}
