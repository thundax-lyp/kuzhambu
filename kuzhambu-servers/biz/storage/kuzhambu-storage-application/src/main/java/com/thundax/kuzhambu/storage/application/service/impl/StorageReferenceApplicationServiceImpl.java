package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.storage.application.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.query.ListStorageReferencesQuery;
import com.thundax.kuzhambu.storage.application.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageReferenceApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StorageReferenceApplicationServiceImpl implements StorageReferenceApplicationService {

    private final StorageApplicationService storageApplicationService;

    public StorageReferenceApplicationServiceImpl(StorageApplicationService storageApplicationService) {
        this.storageApplicationService = storageApplicationService;
    }

    @Override
    public List<StoredObjectReference> list(ListStorageReferencesQuery query) {
        StorageQuery storageQuery = new StorageQuery();
        storageQuery.setId(query == null ? null : query.getId());
        return storageApplicationService.listReferences(storageQuery);
    }

    @Override
    public void addReferences(AddStorageReferencesCommand command) {
        storageApplicationService.addReferences(command);
    }

    @Override
    public int removeReferences(RemoveStorageReferencesCommand command) {
        return storageApplicationService.removeReferences(command);
    }

    @Override
    public int changeReferenceStatus(ChangeStorageReferenceStatusCommand command) {
        return storageApplicationService.changeReferenceStatus(command);
    }
}
