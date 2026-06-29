package com.thundax.kuzhambu.storage.domain.object.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.time.Instant;
import java.util.List;

public interface StoredObjectRepository {

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

    PageResult<StoredObject> page(
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

    int physicalDeleteById(StoredObjectId id);

    List<StoredObject> listExpiredDeletedUnreferenced(Instant storedBefore);

    List<String> listMimeTypes();

    int updateObjectStatus(StoredObject storage);

    int updateReferenceStatus(StoredObject storage);
}
