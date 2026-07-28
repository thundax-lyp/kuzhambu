package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.application.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StorageOwnerBindingFacadeAssembler {

    public AddStorageReferencesCommand toAddReferencesCommand(BindStorageOwnerFacadeRequest request) {
        if (request == null) {
            return null;
        }
        List<Long> storageObjectIds =
                request.getStorageObjectIds() == null ? Collections.emptyList() : request.getStorageObjectIds();
        StorageOwnerType ownerType = toOwnerType(request);
        List<StoredObjectReference> references = storageObjectIds.stream()
                .map(storageObjectId -> new StoredObjectReference(
                        storageObjectId == null ? null : StoredObjectIdCodec.toDomain(storageObjectId),
                        request.getOwnerId(),
                        ownerType == null ? null : ownerType.value(),
                        request.getOwnerParams()))
                .collect(Collectors.toList());
        return new AddStorageReferencesCommand(references);
    }

    public RemoveStorageReferencesCommand toRemoveReferencesCommand(UnbindStorageOwnerFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new RemoveStorageReferencesCommand(
                isBlank(request.getOwnerType()) ? null : StorageOwnerType.from(request.getOwnerType()),
                request.getOwnerId());
    }

    public StorageOwnerType toOwnerType(BindStorageOwnerFacadeRequest request) {
        if (request == null || isBlank(request.getOwnerType())) {
            return null;
        }
        return StorageOwnerType.from(request.getOwnerType());
    }

    public ChangeStorageReferenceStatusCommand toReferencedCommand(MarkStorageUsageFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new ChangeStorageReferenceStatusCommand(
                request.getStorageObjectId() == null
                        ? null
                        : StoredObjectIdCodec.toDomain(request.getStorageObjectId()),
                StoredObjectReferenceStatus.REFERENCED);
    }

    public ChangeStorageReferenceStatusCommand toUnreferencedCommand(MarkStorageUsageFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new ChangeStorageReferenceStatusCommand(
                request.getStorageObjectId() == null
                        ? null
                        : StoredObjectIdCodec.toDomain(request.getStorageObjectId()),
                StoredObjectReferenceStatus.UNREFERENCED);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
