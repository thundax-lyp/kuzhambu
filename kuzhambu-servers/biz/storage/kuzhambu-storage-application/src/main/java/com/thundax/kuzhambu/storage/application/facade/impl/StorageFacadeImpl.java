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
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.ListStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.ListStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
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
    public boolean exists(OpenStorageFacadeRequest request) {
        return request != null
                && storageApplicationService.existsReadableContent(readableContentFacadeAssembler.toQuery(request));
    }

    @Override
    @Transactional(readOnly = true)
    public OpenStorageFacadeResponse open(OpenStorageFacadeRequest request) {
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
    @Transactional(readOnly = true)
    public ListStorageFacadeResponse list(ListStorageFacadeRequest request) {
        return readableContentFacadeAssembler.toListResponse(
                storageApplicationService.list(readableContentFacadeAssembler.toQuery(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadStorageFacadeResponse upload(UploadStorageFacadeRequest request) {
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
                request.getOwnerId(),
                uploadFacadeAssembler.toObjectStatus(request),
                uploadFacadeAssembler.toReferenceStatus(request),
                request.getRemarks()));
        if (result.hasError()) {
            throw new BizException(result.getError());
        }
        return uploadFacadeAssembler.toResponse(result.getStorage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(RemoveStorageFacadeRequest request) {
        if (request == null || request.getStorageObjectId() == null) {
            return;
        }
        storageApplicationService.remove(StoredObjectId.of(request.getStorageObjectId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindOwner(BindStorageOwnerFacadeRequest request) {
        storageApplicationService.addReferences(ownerBindingFacadeAssembler.toAddReferencesCommand(request));
        List<Long> storageObjectIds = request == null || request.getStorageObjectIds() == null
                ? Collections.emptyList()
                : request.getStorageObjectIds();
        for (Long storageObjectId : storageObjectIds) {
            if (storageObjectId == null) {
                continue;
            }
            storageApplicationService.changeReferenceStatus(
                    ownerBindingFacadeAssembler.toReferencedCommand(MarkStorageUsageFacadeRequest.builder()
                            .storageObjectId(storageObjectId)
                            .build()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindOwner(UnbindStorageOwnerFacadeRequest request) {
        storageApplicationService.removeReferences(ownerBindingFacadeAssembler.toRemoveReferencesCommand(request));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markInUse(MarkStorageUsageFacadeRequest request) {
        storageApplicationService.changeReferenceStatus(ownerBindingFacadeAssembler.toReferencedCommand(request));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markUnused(MarkStorageUsageFacadeRequest request) {
        storageApplicationService.changeReferenceStatus(ownerBindingFacadeAssembler.toUnreferencedCommand(request));
    }
}
