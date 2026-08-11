package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import java.io.InputStream;
import java.util.List;

public record UploadStorageObjectCommand(
        InputStream inputStream,
        String originalFilename,
        String contentType,
        StorageByteSize size,
        List<String> allowedSuffixes,
        StorageOwnerRef ownerRef,
        StoredObjectStatus objectStatus,
        StoredObjectReferenceStatus referenceStatus,
        String remarks) {}
