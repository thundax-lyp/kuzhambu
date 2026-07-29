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
import com.thundax.kuzhambu.storage.application.query.StorageObjectPageQuery;
import com.thundax.kuzhambu.storage.application.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
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
        return storageApplicationService.get(query == null ? null : query.getId());
    }

    @Override
    public List<StoredObject> list(ListStorageObjectsQuery query) {
        return storageApplicationService.list(toStorageQuery(query));
    }

    @Override
    public PageResult<StoredObject> page(StorageObjectPageQuery query) {
        return storageApplicationService.page(toStorageQuery(query), toPageQuery(query));
    }

    @Override
    public StoredObject create(CreateStorageCommand command) {
        StoredObjectId id = storageApplicationService.create(command);
        return storageApplicationService.get(id);
    }

    @Override
    public void change(ChangeStorageCommand command) {
        storageApplicationService.change(command);
    }

    @Override
    public int remove(RemoveStorageObjectCommand command) {
        return storageApplicationService.remove(command == null ? null : command.getId());
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
        StorageQuery storageQuery = new StorageQuery();
        storageQuery.setIds(query.getIds());
        storageQuery.setContentType(
                query.getMimeType() == null ? null : query.getMimeType().value());
        storageQuery.setObjectStatus(query.getObjectStatus());
        storageQuery.setReferenceStatus(query.getReferenceStatus());
        applyReferenceOwner(storageQuery, query.getReferenceOwnerRef());
        storageQuery.setOriginalFilename(query.getOriginalFilename());
        storageQuery.setRemarks(query.getRemarks());
        storageQuery.setSortDirection(query.getSortDirection());
        return storageQuery;
    }

    private StorageQuery toStorageQuery(StorageObjectPageQuery query) {
        if (query == null) {
            return null;
        }
        StorageQuery storageQuery = new StorageQuery();
        storageQuery.setContentType(
                query.getMimeType() == null ? null : query.getMimeType().value());
        storageQuery.setObjectStatus(query.getObjectStatus());
        storageQuery.setReferenceStatus(query.getReferenceStatus());
        applyReferenceOwner(storageQuery, query.getReferenceOwnerRef());
        storageQuery.setOriginalFilename(query.getOriginalFilename());
        storageQuery.setRemarks(query.getRemarks());
        storageQuery.setSortDirection(query.getSortDirection());
        return storageQuery;
    }

    private PageQuery toPageQuery(StorageObjectPageQuery query) {
        return query == null ? new PageQuery() : new PageQuery(query.getPageNo(), query.getPageSize());
    }

    private void applyReferenceOwner(StorageQuery query, StorageOwnerRef ownerRef) {
        if (query == null || ownerRef == null) {
            return;
        }
        query.setReferenceOwnerId(ownerRef.ownerId());
        query.setReferenceOwnerType(ownerRef.ownerTypeValue());
    }
}
