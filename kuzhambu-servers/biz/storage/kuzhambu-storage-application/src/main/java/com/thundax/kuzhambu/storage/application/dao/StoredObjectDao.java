package com.thundax.kuzhambu.storage.application.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.model.valueobject.StoredObjectId;
import java.util.List;

public interface StoredObjectDao {

    StoredObject getById(StoredObjectId id);

    List<StoredObject> listByIds(List<Long> idList);

    List<StoredObject> list(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks,
            SortDirection sortDirection);

    Page<StoredObject> page(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks,
            SortDirection sortDirection,
            int pageNo,
            int pageSize);

    StoredObjectId insert(StoredObject entity);

    int update(StoredObject entity);

    int maxPriority();

    int updatePriority(StoredObjectId id, int priority);

    int deleteById(StoredObjectId id);

    List<String> listMimeTypes();

    int updateObjectStatus(StoredObject storage);

    int updateReferenceStatus(StoredObject storage);
}
