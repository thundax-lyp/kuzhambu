package com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsTaskDetailResponse {
    private Long snapshotId;
    private String sourceDomain;
    private String taskType;
    private String taskKey;
    private String taskStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String failureReason;
    private Long requestedByUserId;
    private Instant startedAt;
    private Instant completedAt;
    private Instant snapshotAt;
}
