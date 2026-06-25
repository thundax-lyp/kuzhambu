package com.thundax.kuzhambu.operations.domain.restore.repository;

import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;

public interface RestoreRepository {

    RestoreRecord getById(RestoreId id);

    RestoreId insert(RestoreRecord record);

    int update(RestoreRecord record);

    int deleteById(RestoreId id);
}
