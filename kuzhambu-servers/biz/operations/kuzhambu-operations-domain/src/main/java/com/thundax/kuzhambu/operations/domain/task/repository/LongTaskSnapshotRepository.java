package com.thundax.kuzhambu.operations.domain.task.repository;

import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;

public interface LongTaskSnapshotRepository {

    LongTaskSnapshot getById(LongTaskSnapshotId id);

    LongTaskSnapshotId insert(LongTaskSnapshot snapshot);

    int update(LongTaskSnapshot snapshot);

    int deleteById(LongTaskSnapshotId id);
}
