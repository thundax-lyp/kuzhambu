package com.thundax.kuzhambu.operations.infra.restore.persistence.assembler;

import com.thundax.kuzhambu.operations.domain.restore.codec.RestoreIdCodec;
import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.infra.restore.persistence.dataobject.RestoreDO;
import java.util.ArrayList;
import java.util.List;

public final class RestorePersistenceAssembler {

    private RestorePersistenceAssembler() {}

    public static RestoreDO toObject(RestoreRecord entity) {
        return entity == null
                ? null
                : new RestoreDO(
                        null,
                        RestoreIdCodec.toValue(entity.getId()),
                        entity.getBackupId(),
                        entity.getPreRestoreBackupId(),
                        entity.getRestoreStatus(),
                        entity.getWriteBlockEnabled(),
                        entity.getFailureReason(),
                        entity.getRequesterUserId(),
                        entity.getStartedAt(),
                        entity.getCompletedAt());
    }

    public static RestoreRecord toDomain(RestoreDO dataObject) {
        return dataObject == null
                ? null
                : new RestoreRecord(
                        RestoreIdCodec.toDomain(dataObject.getRestoreId()),
                        dataObject.getBackupId(),
                        dataObject.getPreRestoreBackupId(),
                        dataObject.getRestoreStatus(),
                        dataObject.getWriteBlockEnabled(),
                        dataObject.getFailureReason(),
                        dataObject.getRequesterUserId(),
                        dataObject.getStartedAt(),
                        dataObject.getCompletedAt());
    }

    public static List<RestoreRecord> toDomainList(List<RestoreDO> dataObjects) {
        List<RestoreRecord> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toDomain(item)));
        }
        return entities;
    }
}
