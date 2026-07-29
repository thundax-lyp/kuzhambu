package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.application.query.GetReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery;
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
import com.thundax.kuzhambu.storage.facade.response.ListStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class StorageReadableContentFacadeAssembler {

    public GetReadableStorageContentQuery toReadableContentQuery(OpenStorageFacadeRequest request) {
        if (request == null) {
            return null;
        }
        return new GetReadableStorageContentQuery(
                toStoredObjectId(request),
                StringUtils.isBlank(request.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()),
                toOwnerRef(request.getOwnerType(), request.getOwnerId()));
    }

    public OpenReadableStorageContentQuery toOpenReadableContentQuery(OpenStorageFacadeRequest request) {
        return request == null ? null : new OpenReadableStorageContentQuery(toStoredObjectId(request));
    }

    public StoredObjectId toStoredObjectId(OpenStorageFacadeRequest request) {
        if (request == null || request.getStorageObjectId() == null) {
            return null;
        }
        return StoredObjectIdCodec.toDomain(request.getStorageObjectId());
    }

    public ListStorageObjectsQuery toListStorageObjectsQuery(ListStorageFacadeRequest request) {
        if (request == null) {
            return null;
        }
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

    public OpenStorageFacadeResponse toResponse(StoredObjectContentResult content) {
        if (content == null) {
            return null;
        }
        return OpenStorageFacadeResponse.builder()
                .storedObject(toDto(content.getStorage()))
                .inputStream(content.getInputStream())
                .build();
    }

    public ListStorageFacadeResponse toListResponse(List<StoredObject> storages) {
        List<StorageObjectFacadeDto> storedObjects = storages == null
                ? Collections.emptyList()
                : storages.stream().map(this::toDto).collect(Collectors.toList());
        return ListStorageFacadeResponse.builder().storedObjects(storedObjects).build();
    }

    public StorageObjectFacadeDto toDto(StoredObject storage) {
        if (storage == null) {
            return null;
        }
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
