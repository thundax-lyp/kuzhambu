package com.thundax.kuzhambu.operations.application.task.result;

import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsTaskPageResult {

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
    private Date startedAt;
    private Date completedAt;
    private Date snapshotAt;
}
