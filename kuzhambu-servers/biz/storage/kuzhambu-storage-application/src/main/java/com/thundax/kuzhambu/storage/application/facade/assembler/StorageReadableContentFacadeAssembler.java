package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.dto.ReadableStoredObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.GetReadableContentFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.GetReadableContentFacadeResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class StorageReadableContentFacadeAssembler {

    public StorageQuery toQuery(GetReadableContentFacadeRequest request) {
        if (request == null) {
            return null;
        }
        StorageQuery query = new StorageQuery();
        query.setId(toStoredObjectId(request));
        query.setOwnerId(request.getOwnerId());
        query.setOwnerType(
                StringUtils.isBlank(request.getOwnerType()) ? null : StorageOwnerType.from(request.getOwnerType()));
        query.setReferenceStatus(
                StringUtils.isBlank(request.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(request.getReferenceStatus()));
        return query;
    }

    public StoredObjectId toStoredObjectId(GetReadableContentFacadeRequest request) {
        if (request == null || request.getStorageObjectId() == null) {
            return null;
        }
        return StoredObjectId.of(request.getStorageObjectId());
    }

    public GetReadableContentFacadeResponse toResponse(StoredObjectContent content) {
        if (content == null) {
            return null;
        }
        return GetReadableContentFacadeResponse.builder()
                .storedObject(toDto(content.getStorage()))
                .inputStream(content.getInputStream())
                .build();
    }

    private ReadableStoredObjectFacadeDto toDto(StoredObject storage) {
        if (storage == null) {
            return null;
        }
        return ReadableStoredObjectFacadeDto.builder()
                .id(storage.getId() == null ? null : storage.getId().value())
                .originalFilename(storage.getOriginalFilename())
                .contentType(storage.getContentType())
                .ownerId(storage.getOwnerId())
                .ownerType(
                        storage.getOwnerType() == null
                                ? null
                                : storage.getOwnerType().value())
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
}
