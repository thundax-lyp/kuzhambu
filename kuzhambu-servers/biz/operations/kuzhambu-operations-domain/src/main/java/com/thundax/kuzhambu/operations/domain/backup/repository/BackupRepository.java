package com.thundax.kuzhambu.operations.domain.backup.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;

public interface BackupRepository {

    BackupRecord getById(BackupId id);

    BackupRecord getByFileName(String fileName);

    PageResult<BackupRecord> page(
            String backupType, String backupStatus, Long requesterUserId, int pageNo, int pageSize);

    BackupId insert(BackupRecord record);

    int update(BackupRecord record);

    int deleteById(BackupId id);
}
