package com.thundax.kuzhambu.operations.domain.restore.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;

public interface RestoreRepository {

    RestoreRecord getById(RestoreId id);

    PageResult<RestoreRecord> page(
            Long backupId, String restoreMode, String restoreStatus, Long requesterUserId, int pageNo, int pageSize);

    RestoreId insert(RestoreRecord record);

    int update(RestoreRecord record);

    int deleteById(RestoreId id);
}
