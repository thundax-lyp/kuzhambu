package com.thundax.kuzhambu.operations.infra.task.persistence.assembler;

import com.thundax.kuzhambu.operations.domain.task.codec.LongTaskSnapshotIdCodec;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.infra.task.persistence.dataobject.LongTaskSnapshotDO;
import java.util.ArrayList;
import java.util.List;

public final class LongTaskSnapshotPersistenceAssembler {

    private LongTaskSnapshotPersistenceAssembler() {}

    public static LongTaskSnapshotDO toObject(LongTaskSnapshot entity) {
        return entity == null
                ? null
                : new LongTaskSnapshotDO(
                        null,
                        LongTaskSnapshotIdCodec.toValue(entity.getId()),
                        entity.getSourceDomain(),
                        entity.getTaskType(),
                        entity.getTaskKey(),
                        entity.getTaskStatus(),
                        entity.getTotalCount(),
                        entity.getSuccessCount(),
                        entity.getFailedCount(),
                        entity.getFailureReason(),
                        entity.getRequestedByUserId(),
                        entity.getStartedAt(),
                        entity.getCompletedAt(),
                        entity.getSnapshotAt());
    }

    public static LongTaskSnapshot toDomain(LongTaskSnapshotDO dataObject) {
        return dataObject == null
                ? null
                : new LongTaskSnapshot(
                        LongTaskSnapshotIdCodec.toDomain(dataObject.getSnapshotId()),
                        dataObject.getSourceDomain(),
                        dataObject.getTaskType(),
                        dataObject.getTaskKey(),
                        dataObject.getTaskStatus(),
                        dataObject.getTotalCount(),
                        dataObject.getSuccessCount(),
                        dataObject.getFailedCount(),
                        dataObject.getFailureReason(),
                        dataObject.getRequestedByUserId(),
                        dataObject.getStartedAt(),
                        dataObject.getCompletedAt(),
                        dataObject.getSnapshotAt());
    }

    public static List<LongTaskSnapshot> toDomainList(List<LongTaskSnapshotDO> dataObjects) {
        List<LongTaskSnapshot> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toDomain(item)));
        }
        return entities;
    }
}
