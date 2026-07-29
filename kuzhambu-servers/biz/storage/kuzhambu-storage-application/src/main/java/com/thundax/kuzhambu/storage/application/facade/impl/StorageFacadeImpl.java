package com.thundax.kuzhambu.storage.application.facade.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageOwnerBindingFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageReadableContentFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageUploadFacadeAssembler;
import com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageReferencesQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageMultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageReferenceApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.AbortMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.CompleteMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.InitMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.ListStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadMultipartPartFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.AbortMultipartUploadFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.CompleteMultipartUploadFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.InitMultipartUploadFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.ListStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadMultipartPartFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageFacadeImpl implements StorageFacade {

    private final StorageObjectApplicationService storageObjectApplicationService;
    private final StorageReferenceApplicationService storageReferenceApplicationService;
    private final StorageContentApplicationService storageContentApplicationService;
    private final StorageUploadApplicationService storageUploadApplicationService;
    private final StorageMultipartUploadApplicationService storageMultipartUploadApplicationService;
    private final StorageReadableContentFacadeAssembler readableContentFacadeAssembler;
    private final StorageOwnerBindingFacadeAssembler ownerBindingFacadeAssembler;
    private final StorageUploadFacadeAssembler uploadFacadeAssembler;

    public StorageFacadeImpl(
            StorageObjectApplicationService storageObjectApplicationService,
            StorageReferenceApplicationService storageReferenceApplicationService,
            StorageContentApplicationService storageContentApplicationService,
            StorageUploadApplicationService storageUploadApplicationService,
            StorageMultipartUploadApplicationService storageMultipartUploadApplicationService,
            StorageReadableContentFacadeAssembler readableContentFacadeAssembler,
            StorageOwnerBindingFacadeAssembler ownerBindingFacadeAssembler,
            StorageUploadFacadeAssembler uploadFacadeAssembler) {
        this.storageObjectApplicationService = storageObjectApplicationService;
        this.storageReferenceApplicationService = storageReferenceApplicationService;
        this.storageContentApplicationService = storageContentApplicationService;
        this.storageUploadApplicationService = storageUploadApplicationService;
        this.storageMultipartUploadApplicationService = storageMultipartUploadApplicationService;
        this.readableContentFacadeAssembler = readableContentFacadeAssembler;
        this.ownerBindingFacadeAssembler = ownerBindingFacadeAssembler;
        this.uploadFacadeAssembler = uploadFacadeAssembler;
    }

    @Override
    public boolean exists(OpenStorageFacadeRequest request) {
        return request != null
                && storageContentApplicationService.existsReadableContent(
                        readableContentFacadeAssembler.toReadableContentQuery(request));
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
        StoredObjectContentResult content = storageContentApplicationService.openReadableContent(
                readableContentFacadeAssembler.toOpenReadableContentQuery(request));
        return readableContentFacadeAssembler.toResponse(content);
    }

    @Override
    @Transactional(readOnly = true)
    public ListStorageFacadeResponse list(ListStorageFacadeRequest request) {
        return readableContentFacadeAssembler.toListResponse(storageObjectApplicationService.list(
                readableContentFacadeAssembler.toListStorageObjectsQuery(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadStorageFacadeResponse upload(UploadStorageFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return uploadFacadeAssembler.toResponse(
                storageUploadApplicationService.upload(uploadFacadeAssembler.toUploadStorageObjectCommand(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InitMultipartUploadFacadeResponse initMultipartUpload(InitMultipartUploadFacadeRequest request) {
        return uploadFacadeAssembler.toResponse(storageMultipartUploadApplicationService.init(
                uploadFacadeAssembler.toInitMultipartUploadCommand(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadMultipartPartFacadeResponse uploadPart(UploadMultipartPartFacadeRequest request) {
        return uploadFacadeAssembler.toResponse(storageMultipartUploadApplicationService.uploadPart(
                uploadFacadeAssembler.toUploadMultipartPartCommand(request)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompleteMultipartUploadFacadeResponse completeMultipart(CompleteMultipartUploadFacadeRequest request) {
        return uploadFacadeAssembler.toResponse(
                storageMultipartUploadApplicationService.complete(
                        uploadFacadeAssembler.toCompleteMultipartUploadCommand(request)),
                request == null ? null : request.getUploadId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AbortMultipartUploadFacadeResponse abortMultipart(AbortMultipartUploadFacadeRequest request) {
        storageMultipartUploadApplicationService.abort(uploadFacadeAssembler.toAbortMultipartUploadCommand(request));
        return uploadFacadeAssembler.toResponse(request == null ? null : request.getUploadId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(RemoveStorageFacadeRequest request) {
        if (request == null || request.getStorageObjectId() == null) {
            return;
        }
        storageObjectApplicationService.remove(
                new RemoveStorageObjectCommand(StoredObjectIdCodec.toDomain(request.getStorageObjectId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindOwner(BindStorageOwnerFacadeRequest request) {
        List<Long> storageObjectIds = request == null || request.getStorageObjectIds() == null
                ? Collections.emptyList()
                : request.getStorageObjectIds();
        for (Long storageObjectId : storageObjectIds) {
            bindOwner(storageObjectId, request);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindOwner(UnbindStorageOwnerFacadeRequest request) {
        storageReferenceApplicationService.removeReferences(
                ownerBindingFacadeAssembler.toRemoveReferencesCommand(request));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markInUse(MarkStorageUsageFacadeRequest request) {
        storageReferenceApplicationService.changeReferenceStatus(
                ownerBindingFacadeAssembler.toReferencedCommand(request));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markUnused(MarkStorageUsageFacadeRequest request) {
        storageReferenceApplicationService.changeReferenceStatus(
                ownerBindingFacadeAssembler.toUnreferencedCommand(request));
    }

    private void bindOwner(Long storageObjectId, BindStorageOwnerFacadeRequest request) {
        if (storageObjectId == null || request == null) {
            return;
        }
        requireStoredObject(storageObjectId);
        StorageOwnerType ownerType = ownerBindingFacadeAssembler.toOwnerType(request);
        String ownerId = request.getOwnerId();
        addReferenceIfAbsent(storageObjectId, ownerType, ownerId, request.getOwnerParams());
        storageReferenceApplicationService.changeReferenceStatus(
                ownerBindingFacadeAssembler.toReferencedCommand(MarkStorageUsageFacadeRequest.builder()
                        .storageObjectId(storageObjectId)
                        .build()));
    }

    private StoredObject requireStoredObject(Long storageObjectId) {
        StoredObject storedObject = storageObjectApplicationService.get(
                new GetStorageObjectQuery(StoredObjectIdCodec.toDomain(storageObjectId)));
        if (storedObject == null) {
            throw new BizException("Storage 对象不存在");
        }
        return storedObject;
    }

    private void addReferenceIfAbsent(
            Long storageObjectId, StorageOwnerType ownerType, String ownerId, String ownerParams) {
        if (referenceExists(storageObjectId, ownerType, ownerId)) {
            return;
        }
        storageReferenceApplicationService.addReferences(
                ownerBindingFacadeAssembler.toAddReferencesCommand(BindStorageOwnerFacadeRequest.builder()
                        .storageObjectIds(List.of(storageObjectId))
                        .ownerType(ownerType == null ? null : ownerType.value())
                        .ownerId(ownerId)
                        .ownerParams(ownerParams)
                        .build()));
    }

    private boolean referenceExists(Long storageObjectId, StorageOwnerType ownerType, String ownerId) {
        return listReferences(StoredObjectIdCodec.toDomain(storageObjectId)).stream()
                .anyMatch(reference -> ownerType != null
                        && ownerType.value().equals(reference.getReferenceOwnerType())
                        && ownerId != null
                        && ownerId.equals(reference.getReferenceOwnerId()));
    }

    private List<StoredObjectReference> listReferences(StoredObjectId storedObjectId) {
        if (storedObjectId == null) {
            return Collections.emptyList();
        }
        List<StoredObjectReference> references =
                storageReferenceApplicationService.list(new ListStorageReferencesQuery(storedObjectId));
        return references == null ? Collections.emptyList() : references;
    }
}
