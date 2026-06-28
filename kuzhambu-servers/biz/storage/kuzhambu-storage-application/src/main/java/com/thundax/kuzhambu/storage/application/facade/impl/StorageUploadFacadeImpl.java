package com.thundax.kuzhambu.storage.application.facade.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageUploadFacadeAssembler;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.facade.StorageUploadFacade;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageUploadFacadeImpl implements StorageUploadFacade {

    private final StorageUploadStreamHelper storageUploadStreamHelper;
    private final StorageUploadFacadeAssembler facadeAssembler;

    public StorageUploadFacadeImpl(
            StorageUploadStreamHelper storageUploadStreamHelper, StorageUploadFacadeAssembler facadeAssembler) {
        this.storageUploadStreamHelper = storageUploadStreamHelper;
        this.facadeAssembler = facadeAssembler;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadStorageObjectFacadeResponse uploadStorageObject(UploadStorageObjectFacadeRequest request) {
        if (request == null) {
            return null;
        }
        StorageUploadResult result = storageUploadStreamHelper.upload(
                request.getInputStream(),
                request.getOriginalFilename(),
                request.getContentType(),
                request.getSizeBytes() == null ? 0L : request.getSizeBytes(),
                request.getAllowedSuffixes(),
                facadeAssembler.toOwnerType(request),
                request.getOwnerId());
        if (result.hasError()) {
            throw new BizException(result.getError());
        }
        return facadeAssembler.toResponse(result.getStorage());
    }
}
