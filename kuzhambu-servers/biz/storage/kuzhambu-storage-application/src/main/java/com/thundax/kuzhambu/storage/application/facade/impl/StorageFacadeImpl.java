package com.thundax.kuzhambu.storage.application.facade.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageOwnerBindingFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageReadableContentFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageUploadFacadeAssembler;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.application.service.result.StorageUploadResult;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.BindStorageObjectOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.GetReadableContentFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageObjectUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageObjectOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.GetReadableContentFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageFacadeImpl implements StorageFacade {

    private final StorageApplicationService storageApplicationService;
    private final StorageReadableContentFacadeAssembler readableContentFacadeAssembler;
    private final StorageOwnerBindingFacadeAssembler ownerBindingFacadeAssembler;
    private final StorageUploadFacadeAssembler uploadFacadeAssembler;

    public StorageFacadeImpl(
            StorageApplicationService storageApplicationService,
            StorageReadableContentFacadeAssembler readableContentFacadeAssembler,
            StorageOwnerBindingFacadeAssembler ownerBindingFacadeAssembler,
            StorageUploadFacadeAssembler uploadFacadeAssembler) {
        this.storageApplicationService = storageApplicationService;
        this.readableContentFacadeAssembler = readableContentFacadeAssembler;
        this.ownerBindingFacadeAssembler = ownerBindingFacadeAssembler;
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
        StorageUploadResult result = storageApplicationService.upload(new UploadStorageObjectCommand(
                request.getInputStream(),
                request.getOriginalFilename(),
                request.getContentType(),
                request.getSizeBytes() == null ? 0L : request.getSizeBytes(),
                request.getAllowedSuffixes(),
                uploadFacadeAssembler.toOwnerType(request),
                request.getOwnerId()));
        if (result.hasError()) {
            throw new BizException(result.getError());
        }
        return uploadFacadeAssembler.toResponse(result.getStorage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindOwner(BindStorageObjectOwnerFacadeRequest request) {
        storageApplicationService.addReferences(ownerBindingFacadeAssembler.toAddReferencesCommand(request));
        List<Long> storageObjectIds = request == null || request.getStorageObjectIds() == null
                ? Collections.emptyList()
                : request.getStorageObjectIds();
        for (Long storageObjectId : storageObjectIds) {
            if (storageObjectId == null) {
                continue;
            }
            storageApplicationService.changeReferenceStatus(
                    ownerBindingFacadeAssembler.toReferencedCommand(MarkStorageObjectUsageFacadeRequest.builder()
                            .storageObjectId(storageObjectId)
                            .build()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindOwner(UnbindStorageObjectOwnerFacadeRequest request) {
        storageApplicationService.removeReferences(ownerBindingFacadeAssembler.toRemoveReferencesCommand(request));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markInUse(MarkStorageObjectUsageFacadeRequest request) {
        storageApplicationService.changeReferenceStatus(ownerBindingFacadeAssembler.toReferencedCommand(request));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markUnused(MarkStorageObjectUsageFacadeRequest request) {
        storageApplicationService.changeReferenceStatus(ownerBindingFacadeAssembler.toUnreferencedCommand(request));
    }
}
