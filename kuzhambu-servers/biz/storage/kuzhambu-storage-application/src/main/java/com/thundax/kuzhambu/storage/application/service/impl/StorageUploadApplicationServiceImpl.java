package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import org.springframework.stereotype.Service;

@Service
public class StorageUploadApplicationServiceImpl implements StorageUploadApplicationService {

    private final StorageApplicationServiceImpl storageApplicationService;

    public StorageUploadApplicationServiceImpl(StorageApplicationServiceImpl storageApplicationService) {
        this.storageApplicationService = storageApplicationService;
    }

    @Override
    public StoredObject upload(UploadStorageObjectCommand command) {
        return storageApplicationService.upload(command);
    }
}
