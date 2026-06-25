package com.thundax.kuzhambu.operations.domain.backup.repository;

import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;

public interface BackupRepository {

    BackupRecord getById(BackupId id);

    BackupId insert(BackupRecord record);

    int update(BackupRecord record);

    int deleteById(BackupId id);
}
