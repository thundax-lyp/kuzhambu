package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.storage.application.query.GetReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
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
    public StoredObjectContentResult openReadableContent(OpenReadableStorageContentQuery query) {
        return storageApplicationService.openReadableContent(query);
    }

    private StorageQuery toStorageQuery(GetReadableStorageContentQuery query) {
        if (query == null) {
            return null;
        }
        return applyReferenceOwner(
                new StorageQuery(query.id(), null, null, null, null, null, query.referenceStatus(), null, null, null),
                query.referenceOwnerRef());
    }

    private StorageQuery applyReferenceOwner(StorageQuery query, StorageOwnerRef ownerRef) {
        if (query == null || ownerRef == null) {
            return query;
        }
        return new StorageQuery(
                query.id(),
                query.ids(),
                query.contentType(),
                ownerRef.ownerId(),
                ownerRef.ownerTypeValue(),
                query.objectStatus(),
                query.referenceStatus(),
                query.originalFilename(),
                query.remarks(),
                query.sortDirection());
    }
}
