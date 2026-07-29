package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.result.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import org.springframework.stereotype.Service;

@Service
public class StorageUploadApplicationServiceImpl implements StorageUploadApplicationService {

    private final StorageApplicationService storageApplicationService;

    public StorageUploadApplicationServiceImpl(StorageApplicationService storageApplicationService) {
        this.storageApplicationService = storageApplicationService;
    }

    @Override
    public StoredObject upload(UploadStorageObjectCommand command) {
        StorageUploadResult result = storageApplicationService.upload(command);
        if (result == null) {
            return null;
        }
        if (result.hasError()) {
            throw new BizException(result.getError());
        }
        return result.getStorage();
    }
}
