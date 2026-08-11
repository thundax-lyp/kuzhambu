package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.query.GetReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageReferencesQuery;
import com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.ListStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.ListStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StorageReadableContentFacadeAssembler {

    @NonNull
    public RemoveStorageObjectCommand toRemoveStorageObjectCommand(@NonNull RemoveStorageFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new RemoveStorageObjectCommand(toStoredObjectId(request));
    }

    @NonNull
    public GetStorageObjectQuery toGetStorageObjectQuery(@NonNull Long storageObjectId) {
        Objects.requireNonNull(storageObjectId, "storageObjectId must not be null");
        return new GetStorageObjectQuery(StoredObjectIdCodec.toDomain(storageObjectId));
    }

    @NonNull
    public ListStorageReferencesQuery toListStorageReferencesQuery(@NonNull StoredObjectId storageObjectId) {
        Objects.requireNonNull(storageObjectId, "storageObjectId must not be null");
        return new ListStorageReferencesQuery(storageObjectId);
    }

    @NonNull
    public ListStorageReferencesQuery toListStorageReferencesQuery(@NonNull Long storageObjectId) {
        Objects.requireNonNull(storageObjectId, "storageObjectId must not be null");
        return new ListStorageReferencesQuery(toStoredObjectId(storageObjectId));
    }

    @NonNull
    public GetReadableStorageContentQuery toReadableContentQuery(@NonNull OpenStorageFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GetReadableStorageContentQuery(
                toStoredObjectId(request),
                StringUtils.isBlank(request.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()),
                toOwnerRef(request.getOwnerType(), request.getOwnerId()));
    }

    @NonNull
    public OpenReadableStorageContentQuery toOpenReadableContentQuery(@NonNull OpenStorageFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OpenReadableStorageContentQuery(toStoredObjectId(request));
    }

    private StoredObjectId toStoredObjectId(OpenStorageFacadeRequest request) {
        if (request.getStorageObjectId() == null) {
            return null;
        }
        return StoredObjectIdCodec.toDomain(request.getStorageObjectId());
    }

    private StoredObjectId toStoredObjectId(RemoveStorageFacadeRequest request) {
        if (request.getStorageObjectId() == null) {
            return null;
        }
        return StoredObjectIdCodec.toDomain(request.getStorageObjectId());
    }

    private StoredObjectId toStoredObjectId(Long storageObjectId) {
        return StoredObjectIdCodec.toDomain(storageObjectId);
    }

    @NonNull
    public ListStorageObjectsQuery toListStorageObjectsQuery(@NonNull ListStorageFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new ListStorageObjectsQuery(
                null,
                null,
                StringUtils.isBlank(request.getObjectStatus())
                        ? null
                        : StoredObjectStatus.from(request.getObjectStatus()),
                StringUtils.isBlank(request.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()),
                toOwnerRef(request.getOwnerType(), request.getOwnerId()),
                null,
                request.getRemarks(),
                null);
    }

    @NonNull
    public OpenStorageFacadeResponse toResponse(@NonNull StoredObjectContentResult content) {
        Objects.requireNonNull(content, "content must not be null");
        return OpenStorageFacadeResponse.builder()
                .storedObject(content.getStorage() == null ? null : toDto(content.getStorage()))
                .inputStream(content.getInputStream())
                .build();
    }

    @NonNull
    public ListStorageFacadeResponse toListResponse(@NonNull List<StoredObject> storages) {
        Objects.requireNonNull(storages, "storages must not be null");
        List<StorageObjectFacadeDto> storedObjects =
                storages.stream().map(this::toDto).collect(Collectors.toList());
        return ListStorageFacadeResponse.builder().storedObjects(storedObjects).build();
    }

    @NonNull
    public StorageObjectFacadeDto toDto(@NonNull StoredObject storage) {
        Objects.requireNonNull(storage, "storage must not be null");
        return StorageObjectFacadeDto.builder()
                .id(storage.getId() == null ? null : storage.getId().value())
                .originalFilename(storage.getOriginalFilename())
                .contentType(storage.getContentType())
                .ownerId(null)
                .ownerType(null)
                .size(storage.getSize())
                .objectStatus(
                        storage.getObjectStatus() == null
                                ? null
                                : storage.getObjectStatus().value())
                .referenceStatus(
                        storage.getReferenceStatus() == null
                                ? null
                                : storage.getReferenceStatus().value())
                .remarks(storage.getRemarks())
                .build();
    }

    private StorageOwnerRef toOwnerRef(String ownerType, String ownerId) {
        return StorageOwnerRef.ofNullable(
                StringUtils.isBlank(ownerType) ? null : StorageOwnerType.from(ownerType), ownerId);
    }
}
