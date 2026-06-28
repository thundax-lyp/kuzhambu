package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.request.BindStorageObjectOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageObjectUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageObjectOwnerFacadeRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StorageOwnerBindingFacadeAssembler {

    public AddStorageReferencesCommand toAddReferencesCommand(BindStorageObjectOwnerFacadeRequest request) {
        if (request == null) {
            return null;
        }
        List<Long> storageObjectIds =
                request.getStorageObjectIds() == null ? Collections.emptyList() : request.getStorageObjectIds();
        StorageOwnerType ownerType =
                isBlank(request.getOwnerType()) ? null : StorageOwnerType.from(request.getOwnerType());
        List<StoredObjectReference> references = storageObjectIds.stream()
                .map(storageObjectId -> new StoredObjectReference(
                        storageObjectId == null ? null : StoredObjectId.of(storageObjectId),
                        request.getOwnerId(),
                        ownerType,
                        request.getOwnerParams(),
                        StoredObjectReferenceStatus.REFERENCED))
                .collect(Collectors.toList());
        return new AddStorageReferencesCommand(references);
    }

    public RemoveStorageReferencesCommand toRemoveReferencesCommand(UnbindStorageObjectOwnerFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new RemoveStorageReferencesCommand(
                isBlank(request.getOwnerType()) ? null : StorageOwnerType.from(request.getOwnerType()),
                request.getOwnerId());
    }

    public ChangeStorageReferenceStatusCommand toReferencedCommand(MarkStorageObjectUsageFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new ChangeStorageReferenceStatusCommand(
                request.getStorageObjectId() == null ? null : StoredObjectId.of(request.getStorageObjectId()),
                StoredObjectReferenceStatus.REFERENCED);
    }

    public ChangeStorageReferenceStatusCommand toUnreferencedCommand(MarkStorageObjectUsageFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new ChangeStorageReferenceStatusCommand(
                request.getStorageObjectId() == null ? null : StoredObjectId.of(request.getStorageObjectId()),
                StoredObjectReferenceStatus.UNREFERENCED);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
