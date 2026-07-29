package com.thundax.kuzhambu.storage.domain.object.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageReferenceOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.time.Instant;
import java.util.List;

public interface StoredObjectRepository {

    StoredObject getById(StoredObjectId id);

    List<StoredObject> listByIds(List<StoredObjectId> idList);

    List<StoredObject> list(
            StorageMimeType mimeType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String referenceOwnerId,
            StorageReferenceOwnerType referenceOwnerType,
            String name,
            String remarks,
            SortDirection sortDirection);

    PageResult<StoredObject> page(
            StorageMimeType mimeType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String referenceOwnerId,
            StorageReferenceOwnerType referenceOwnerType,
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

    List<StoredObject> listExpiredActiveUnreferenced(Instant storedBefore);

    List<StoredObject> listExpiredDeletedUnreferenced(Instant storedBefore);

    List<StorageMimeType> listMimeTypes();

    int updateObjectStatus(StoredObject storage);

    int updateReferenceStatus(StoredObject storage);
}
