package com.thundax.kuzhambu.operations.application.task.result;

import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsTaskDetailResult {

    private LongTaskSnapshotId snapshotId;
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
