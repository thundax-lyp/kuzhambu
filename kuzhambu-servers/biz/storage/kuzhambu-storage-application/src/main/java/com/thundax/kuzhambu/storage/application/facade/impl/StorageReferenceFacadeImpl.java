package com.thundax.kuzhambu.storage.application.facade.impl;

import com.thundax.kuzhambu.storage.application.facade.assembler.StorageReferenceFacadeAssembler;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.facade.StorageReferenceFacade;
import com.thundax.kuzhambu.storage.facade.request.AddStorageReferencesFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.ChangeStorageReferenceStatusFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageReferencesFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.StorageReferenceFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageReferenceFacadeImpl implements StorageReferenceFacade {

    private final StorageApplicationService storageApplicationService;
    private final StorageReferenceFacadeAssembler facadeAssembler;

    public StorageReferenceFacadeImpl(
            StorageApplicationService storageApplicationService, StorageReferenceFacadeAssembler facadeAssembler) {
        this.storageApplicationService = storageApplicationService;
        this.facadeAssembler = facadeAssembler;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StorageReferenceFacadeResponse addReferences(AddStorageReferencesFacadeRequest request) {
        storageApplicationService.addReferences(facadeAssembler.toCommand(request));
        int affectedCount = request == null || request.getStorageObjectIds() == null
                ? 0
                : request.getStorageObjectIds().size();
        return facadeAssembler.toResponse(affectedCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StorageReferenceFacadeResponse removeReferences(RemoveStorageReferencesFacadeRequest request) {
        return facadeAssembler.toResponse(
                storageApplicationService.removeReferences(facadeAssembler.toCommand(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StorageReferenceFacadeResponse changeReferenceStatus(ChangeStorageReferenceStatusFacadeRequest request) {
        return facadeAssembler.toResponse(
                storageApplicationService.changeReferenceStatus(facadeAssembler.toCommand(request)));
    }
}
