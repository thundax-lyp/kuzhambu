package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.request.AddStorageReferencesFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.ChangeStorageReferenceStatusFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageReferencesFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.StorageReferenceFacadeResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StorageReferenceFacadeAssembler {

    public AddStorageReferencesCommand toCommand(AddStorageReferencesFacadeRequest request) {
        if (request == null) {
            return null;
        }
        List<Long> storageObjectIds =
                request.getStorageObjectIds() == null ? Collections.emptyList() : request.getStorageObjectIds();
        StorageOwnerType ownerType =
                isBlank(request.getOwnerType()) ? null : StorageOwnerType.from(request.getOwnerType());
        StoredObjectReferenceStatus referenceStatus = isBlank(request.getReferenceStatus())
                ? null
                : StoredObjectReferenceStatus.from(request.getReferenceStatus());
        List<StoredObjectReference> references = storageObjectIds.stream()
                .map(storageObjectId -> new StoredObjectReference(
                        storageObjectId == null ? null : StoredObjectId.of(storageObjectId),
                        request.getOwnerId(),
                        ownerType,
                        request.getOwnerParams(),
                        referenceStatus))
                .collect(Collectors.toList());
        return new AddStorageReferencesCommand(references);
    }

    public RemoveStorageReferencesCommand toCommand(RemoveStorageReferencesFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new RemoveStorageReferencesCommand(
                isBlank(request.getOwnerType()) ? null : StorageOwnerType.from(request.getOwnerType()),
                request.getOwnerId());
    }

    public ChangeStorageReferenceStatusCommand toCommand(ChangeStorageReferenceStatusFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new ChangeStorageReferenceStatusCommand(
                request.getStorageObjectId() == null ? null : StoredObjectId.of(request.getStorageObjectId()),
                isBlank(request.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()));
    }

    public StorageReferenceFacadeResponse toResponse(Integer affectedCount) {
        return StorageReferenceFacadeResponse.builder()
                .affectedCount(affectedCount)
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
