package com.thundax.kuzhambu.operations.infra.backup.persistence.assembler;

import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.infra.backup.persistence.dataobject.BackupDO;
import java.util.ArrayList;
import java.util.List;

public final class BackupPersistenceAssembler {

    private BackupPersistenceAssembler() {}

    public static BackupDO toObject(BackupRecord entity) {
        return entity == null
                ? null
                : new BackupDO(
                        null,
                        BackupIdCodec.toValue(entity.getId()),
                        entity.getBackupType(),
                        entity.getBackupStatus(),
                        entity.getStorageObjectId(),
                        entity.getFileName(),
                        entity.getFileSizeBytes(),
                        entity.getChecksum(),
                        entity.getFailureReason(),
                        entity.getRequesterUserId(),
                        entity.getStartedAt(),
                        entity.getCompletedAt(),
                        entity.getExpiresAt());
    }

    public static BackupRecord toDomain(BackupDO dataObject) {
        return dataObject == null
                ? null
                : new BackupRecord(
                        BackupIdCodec.toDomain(dataObject.getBackupId()),
                        dataObject.getBackupType(),
                        dataObject.getBackupStatus(),
                        dataObject.getStorageObjectId(),
                        dataObject.getFileName(),
                        dataObject.getFileSizeBytes(),
                        dataObject.getChecksum(),
                        dataObject.getFailureReason(),
                        dataObject.getRequesterUserId(),
                        dataObject.getStartedAt(),
                        dataObject.getCompletedAt(),
                        dataObject.getExpiresAt());
    }

    public static List<BackupRecord> toDomainList(List<BackupDO> dataObjects) {
        List<BackupRecord> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toDomain(item)));
        }
        return entities;
    }
}
