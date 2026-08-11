package com.thundax.kuzhambu.operations.interfaces.admin.task.assembler;

import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskDetailQuery;
import com.thundax.kuzhambu.operations.application.task.query.OperationsTaskQuery;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskDetailResult;
import com.thundax.kuzhambu.operations.application.task.result.OperationsTaskPageResult;
import com.thundax.kuzhambu.operations.domain.task.codec.LongTaskSnapshotIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.request.OperationsTaskDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.request.OperationsTaskPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response.OperationsTaskDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response.OperationsTaskPageResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class OperationsTaskInterfaceAssembler {

    private OperationsTaskInterfaceAssembler() {}

    @NonNull
    public static OperationsTaskQuery toQuery(@NonNull OperationsTaskPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsTaskQuery(request.getSourceDomain(), request.getTaskType(), request.getTaskStatus());
    }

    @NonNull
    public static OperationsTaskDetailQuery toQuery(@NonNull OperationsTaskDetailRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsTaskDetailQuery(LongTaskSnapshotIdCodec.toDomain(request.getSnapshotId()));
    }

    @NonNull
    public static OperationsTaskPageResponse toResponse(@NonNull OperationsTaskPageResult result) {
        Objects.requireNonNull(result, "result must not be null");
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

    @NonNull
    public static OperationsTaskDetailResponse toDetailResponse(@NonNull OperationsTaskDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
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
