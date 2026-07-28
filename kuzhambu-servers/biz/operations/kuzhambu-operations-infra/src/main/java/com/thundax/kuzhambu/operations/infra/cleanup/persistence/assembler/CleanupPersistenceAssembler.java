package com.thundax.kuzhambu.operations.infra.cleanup.persistence.assembler;

import com.thundax.kuzhambu.operations.domain.cleanup.codec.CleanupItemIdCodec;
import com.thundax.kuzhambu.operations.domain.cleanup.codec.CleanupJobIdCodec;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupItem;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupJob;
import com.thundax.kuzhambu.operations.infra.cleanup.persistence.dataobject.CleanupItemDO;
import com.thundax.kuzhambu.operations.infra.cleanup.persistence.dataobject.CleanupJobDO;
import java.util.ArrayList;
import java.util.List;

public final class CleanupPersistenceAssembler {

    private CleanupPersistenceAssembler() {}

    public static CleanupJobDO toObject(CleanupJob entity) {
        return entity == null
                ? null
                : new CleanupJobDO(
                        null,
                        entity.getId() == null ? null : entity.getId().value(),
                        entity.getCleanupType(),
                        entity.getCleanupStatus(),
                        entity.getTotalCount(),
                        entity.getSuccessCount(),
                        entity.getFailedCount(),
                        entity.getFailureReason(),
                        entity.getRequesterUserId(),
                        entity.getStartedAt(),
                        entity.getCompletedAt());
    }

    public static CleanupJob toDomain(CleanupJobDO dataObject) {
        return dataObject == null
                ? null
                : new CleanupJob(
                        CleanupJobIdCodec.toDomain(dataObject.getCleanupId()),
                        dataObject.getCleanupType(),
                        dataObject.getCleanupStatus(),
                        dataObject.getTotalCount(),
                        dataObject.getSuccessCount(),
                        dataObject.getFailedCount(),
                        dataObject.getFailureReason(),
                        dataObject.getRequesterUserId(),
                        dataObject.getStartedAt(),
                        dataObject.getCompletedAt(),
                        new ArrayList<>());
    }

    public static CleanupItemDO toObject(CleanupItem entity) {
        return entity == null
                ? null
                : new CleanupItemDO(
                        null,
                        entity.getId() == null ? null : entity.getId().value(),
                        entity.getCleanupId(),
                        entity.getTargetType(),
                        entity.getTargetId(),
                        entity.getItemStatus(),
                        entity.getFailureReason(),
                        entity.getProcessedAt());
    }

    public static CleanupItem toDomain(CleanupItemDO dataObject) {
        return dataObject == null
                ? null
                : new CleanupItem(
                        CleanupItemIdCodec.toDomain(dataObject.getCleanupItemId()),
                        dataObject.getCleanupId(),
                        dataObject.getTargetType(),
                        dataObject.getTargetId(),
                        dataObject.getItemStatus(),
                        dataObject.getFailureReason(),
                        dataObject.getProcessedAt());
    }

    public static List<CleanupItem> toDomainList(List<CleanupItemDO> dataObjects) {
        List<CleanupItem> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toDomain(item)));
        }
        return entities;
    }
}
