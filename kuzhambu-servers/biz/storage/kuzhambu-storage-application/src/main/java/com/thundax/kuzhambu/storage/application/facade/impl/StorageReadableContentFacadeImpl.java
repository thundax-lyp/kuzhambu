package com.thundax.kuzhambu.storage.application.facade.impl;

import com.thundax.kuzhambu.storage.application.facade.assembler.StorageReadableContentFacadeAssembler;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageReadableContentFacade;
import com.thundax.kuzhambu.storage.facade.request.GetReadableContentFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.GetReadableContentFacadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageReadableContentFacadeImpl implements StorageReadableContentFacade {

    private final StorageApplicationService storageApplicationService;
    private final StorageReadableContentFacadeAssembler facadeAssembler;

    public StorageReadableContentFacadeImpl(
            StorageApplicationService storageApplicationService,
            StorageReadableContentFacadeAssembler facadeAssembler) {
        this.storageApplicationService = storageApplicationService;
        this.facadeAssembler = facadeAssembler;
    }

    @Override
    public boolean existsReadableContent(GetReadableContentFacadeRequest request) {
        return request != null && storageApplicationService.existsReadableContent(facadeAssembler.toQuery(request));
    }

    @Override
    @Transactional(readOnly = true)
    public GetReadableContentFacadeResponse getReadableContent(GetReadableContentFacadeRequest request) {
        if (!existsReadableContent(request)) {
            return null;
        }
        StoredObjectId storedObjectId = facadeAssembler.toStoredObjectId(request);
        if (storedObjectId == null) {
            return null;
        }
        StoredObjectContent content = storageApplicationService.openReadableContent(storedObjectId);
        return facadeAssembler.toResponse(content);
    }
}
