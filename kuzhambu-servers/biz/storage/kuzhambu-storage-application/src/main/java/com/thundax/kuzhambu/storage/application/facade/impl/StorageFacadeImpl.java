package com.thundax.kuzhambu.storage.application.facade.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageReadableContentFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageUploadFacadeAssembler;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.GetReadableContentFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.GetReadableContentFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageFacadeImpl implements StorageFacade {

    private final StorageApplicationService storageApplicationService;
    private final StorageReadableContentFacadeAssembler readableContentFacadeAssembler;
    private final StorageUploadStreamHelper storageUploadStreamHelper;
    private final StorageUploadFacadeAssembler uploadFacadeAssembler;

    public StorageFacadeImpl(
            StorageApplicationService storageApplicationService,
            StorageReadableContentFacadeAssembler readableContentFacadeAssembler,
            StorageUploadStreamHelper storageUploadStreamHelper,
            StorageUploadFacadeAssembler uploadFacadeAssembler) {
        this.storageApplicationService = storageApplicationService;
        this.readableContentFacadeAssembler = readableContentFacadeAssembler;
        this.storageUploadStreamHelper = storageUploadStreamHelper;
        this.uploadFacadeAssembler = uploadFacadeAssembler;
    }

    @Override
    public boolean exists(GetReadableContentFacadeRequest request) {
        return request != null
                && storageApplicationService.existsReadableContent(readableContentFacadeAssembler.toQuery(request));
    }

    @Override
    @Transactional(readOnly = true)
    public GetReadableContentFacadeResponse open(GetReadableContentFacadeRequest request) {
        if (!exists(request)) {
            return null;
        }
        StoredObjectId storedObjectId = readableContentFacadeAssembler.toStoredObjectId(request);
        if (storedObjectId == null) {
            return null;
        }
        StoredObjectContent content = storageApplicationService.openReadableContent(storedObjectId);
        return readableContentFacadeAssembler.toResponse(content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadStorageObjectFacadeResponse upload(UploadStorageObjectFacadeRequest request) {
        if (request == null) {
            return null;
        }
        StorageUploadResult result = storageUploadStreamHelper.upload(
                request.getInputStream(),
                request.getOriginalFilename(),
                request.getContentType(),
                request.getSizeBytes() == null ? 0L : request.getSizeBytes(),
                request.getAllowedSuffixes(),
                uploadFacadeAssembler.toOwnerType(request),
                request.getOwnerId());
        if (result.hasError()) {
            throw new BizException(result.getError());
        }
        return uploadFacadeAssembler.toResponse(result.getStorage());
    }
}
