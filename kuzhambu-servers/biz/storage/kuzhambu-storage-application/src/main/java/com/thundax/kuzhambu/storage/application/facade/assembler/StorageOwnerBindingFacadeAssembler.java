package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.application.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StorageOwnerBindingFacadeAssembler {

    @NonNull
    public AddStorageReferencesCommand toAddReferencesCommand(@NonNull BindStorageOwnerFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
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

    @NonNull
    public RemoveStorageReferencesCommand toRemoveReferencesCommand(@NonNull UnbindStorageOwnerFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RemoveStorageReferencesCommand(StorageOwnerRef.ofNullable(
                isBlank(request.getOwnerType()) ? null : StorageOwnerType.from(request.getOwnerType()),
                request.getOwnerId()));
    }

    private StorageOwnerType toOwnerType(BindStorageOwnerFacadeRequest request) {
        if (isBlank(request.getOwnerType())) {
            return null;
        }
        return StorageOwnerType.from(request.getOwnerType());
    }

    @NonNull
    public ChangeStorageReferenceStatusCommand toReferencedCommand(@NonNull MarkStorageUsageFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ChangeStorageReferenceStatusCommand(
                request.getStorageObjectId() == null
                        ? null
                        : StoredObjectIdCodec.toDomain(request.getStorageObjectId()),
                StoredObjectReferenceStatus.REFERENCED);
    }

    @NonNull
    public ChangeStorageReferenceStatusCommand toUnreferencedCommand(@NonNull MarkStorageUsageFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
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
