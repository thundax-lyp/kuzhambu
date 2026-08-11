package com.thundax.kuzhambu.storage.application.query;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.util.List;

public record ListStorageObjectsQuery(
        List<StoredObjectId> ids,
        StorageMimeType mimeType,
        StoredObjectStatus objectStatus,
        StoredObjectReferenceStatus referenceStatus,
        StorageOwnerRef referenceOwnerRef,
        String originalFilename,
        String remarks,
        SortDirection sortDirection) {
    public ListStorageObjectsQuery {
        if (sortDirection == null) {
            sortDirection = SortDirection.ASC;
        }
    }
}
