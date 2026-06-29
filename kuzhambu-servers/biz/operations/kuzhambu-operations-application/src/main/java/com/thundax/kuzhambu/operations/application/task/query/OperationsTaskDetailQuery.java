package com.thundax.kuzhambu.operations.application.task.query;

import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsTaskDetailQuery {

    private LongTaskSnapshotId snapshotId;
}
