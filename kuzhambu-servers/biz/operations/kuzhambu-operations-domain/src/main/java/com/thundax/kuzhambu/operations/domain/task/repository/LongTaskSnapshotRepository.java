package com.thundax.kuzhambu.operations.domain.task.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;

public interface LongTaskSnapshotRepository {

    LongTaskSnapshot getById(LongTaskSnapshotId id);

    PageResult<LongTaskSnapshot> page(
            String sourceDomain, String taskType, String taskStatus, int pageNo, int pageSize);

    LongTaskSnapshotId insert(LongTaskSnapshot snapshot);

    int update(LongTaskSnapshot snapshot);

    int deleteById(LongTaskSnapshotId id);
}
