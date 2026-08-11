package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageBucketName;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageObjectKey;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;

public record ChangeStorageCommand(
        StoredObjectId id,
        String originalFilename,
        String contentType,
        String name,
        String extendName,
        StorageMimeType mimeType,
        StorageOwnerRef ownerRef,
        StorageBucketName bucketName,
        StorageObjectKey objectKey,
        StorageByteSize size,
        String accessEndpoint,
        StoredObjectStatus objectStatus,
        StoredObjectReferenceStatus referenceStatus,
        String remarks) {}
