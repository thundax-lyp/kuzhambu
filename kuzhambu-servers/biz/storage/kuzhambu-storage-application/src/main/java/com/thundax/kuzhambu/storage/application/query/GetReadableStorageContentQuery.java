package com.thundax.kuzhambu.storage.application.query;

import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;

public record GetReadableStorageContentQuery(
        StoredObjectId id, StoredObjectReferenceStatus referenceStatus, StorageOwnerRef referenceOwnerRef) {}
