package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageMimeTypesQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageReferenceOwnerTypesQuery;
import com.thundax.kuzhambu.storage.application.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StorageObjectApplicationServiceImpl implements StorageObjectApplicationService {

    private final StorageApplicationService storageApplicationService;

    public StorageObjectApplicationServiceImpl(StorageApplicationService storageApplicationService) {
        this.storageApplicationService = storageApplicationService;
    }

    @Override
    public StoredObject get(GetStorageObjectQuery query) {
        return storageApplicationService.get(query);
    }

    @Override
    public List<StoredObject> list(ListStorageObjectsQuery query) {
        return storageApplicationService.list(toStorageQuery(query));
    }

    @Override
    public PageResult<StoredObject> page(ListStorageObjectsQuery query, PageQuery pageQuery) {
        return storageApplicationService.page(query, pageQuery);
    }

    @Override
    public StoredObject create(CreateStorageCommand command) {
        return storageApplicationService.create(command);
    }

    @Override
    public void change(ChangeStorageCommand command) {
        storageApplicationService.change(command);
    }

    @Override
    public int remove(RemoveStorageObjectCommand command) {
        return storageApplicationService.remove(command);
    }

    @Override
    public int changeObjectStatus(ChangeStorageObjectStatusCommand command) {
        return storageApplicationService.changeObjectStatus(command);
    }

    @Override
    public void sort(StorageSortCommand command) {
        storageApplicationService.sort(command);
    }

    @Override
    public List<String> listMimeTypes(ListStorageMimeTypesQuery query) {
        return storageApplicationService.listMimeTypes(new StorageQuery());
    }

    @Override
    public List<String> listReferenceOwnerTypes(ListStorageReferenceOwnerTypesQuery query) {
        return storageApplicationService.listReferenceOwnerTypes(new StorageQuery());
    }

    private StorageQuery toStorageQuery(ListStorageObjectsQuery query) {
        if (query == null) {
            return null;
        }
        return applyReferenceOwner(
                new StorageQuery(
                        null,
                        query.ids(),
                        query.mimeType() == null ? null : query.mimeType().value(),
                        null,
                        null,
                        query.objectStatus(),
                        query.referenceStatus(),
                        query.originalFilename(),
                        query.remarks(),
                        query.sortDirection()),
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
