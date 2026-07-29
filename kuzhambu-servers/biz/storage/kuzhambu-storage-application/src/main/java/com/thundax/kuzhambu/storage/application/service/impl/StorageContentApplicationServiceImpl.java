package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.storage.application.query.GetReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import org.springframework.stereotype.Service;

@Service
public class StorageContentApplicationServiceImpl implements StorageContentApplicationService {

    private final StorageApplicationService storageApplicationService;

    public StorageContentApplicationServiceImpl(StorageApplicationService storageApplicationService) {
        this.storageApplicationService = storageApplicationService;
    }

    @Override
    public boolean existsReadableContent(GetReadableStorageContentQuery query) {
        return storageApplicationService.existsReadableContent(toStorageQuery(query));
    }

    @Override
    public StoredObjectContent openReadableContent(OpenReadableStorageContentQuery query) {
        return storageApplicationService.openReadableContent(query == null ? null : query.getId());
    }

    private StorageQuery toStorageQuery(GetReadableStorageContentQuery query) {
        if (query == null) {
            return null;
        }
        StorageQuery storageQuery = new StorageQuery();
        storageQuery.setId(query.getId());
        storageQuery.setReferenceStatus(query.getReferenceStatus());
        applyReferenceOwner(storageQuery, query.getReferenceOwnerRef());
        return storageQuery;
    }

    private void applyReferenceOwner(StorageQuery query, StorageOwnerRef ownerRef) {
        if (query == null || ownerRef == null) {
            return;
        }
        query.setReferenceOwnerId(ownerRef.ownerId());
        query.setReferenceOwnerType(ownerRef.ownerTypeValue());
    }
}
