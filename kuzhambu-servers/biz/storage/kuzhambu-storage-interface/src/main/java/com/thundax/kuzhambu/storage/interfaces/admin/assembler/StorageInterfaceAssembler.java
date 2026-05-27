package com.thundax.kuzhambu.storage.interfaces.admin.assembler;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.interfaces.admin.controller.request.StoragePageRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.controller.response.StorageObjectResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class StorageInterfaceAssembler {

    private StorageInterfaceAssembler() {}

    @NonNull
    public static StorageQuery toQuery(@NonNull StoragePageRequest request) {
        StorageQuery query = new StorageQuery();
        query.setContentType(emptyToNull(request.getContentType()));
        query.setReferenceOwnerId(emptyToNull(request.getReferenceOwnerId()));
        query.setReferenceOwnerType(emptyToNull(request.getReferenceOwnerType()));
        query.setOwnerId(emptyToNull(request.getOwnerId()));
        query.setOwnerType(ownerTypeFrom(request.getOwnerType()));
        query.setObjectStatus(objectStatusFrom(request.getObjectStatus()));
        query.setReferenceStatus(referenceStatusFrom(request.getReferenceStatus()));
        query.setOriginalFilename(emptyToNull(request.getOriginalFilename()));
        query.setRemarks(emptyToNull(request.getRemarks()));
        query.setSortDirection(sortDirectionFrom(request.getSortDirection()));
        return query;
    }

    @NonNull
    public static StorageObjectResponse toResponse(StoredObject entity) {
        if (entity == null) {
            return StorageObjectResponse.builder().build();
        }
        return StorageObjectResponse.builder()
                .id(StoredObjectIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .originalFilename(entity.getOriginalFilename())
                .contentType(entity.getContentType())
                .ownerId(entity.getOwnerId())
                .ownerType(valueOf(entity.getOwnerType()))
                .size(entity.getSize())
                .objectStatus(valueOf(entity.getObjectStatus()))
                .referenceStatus(valueOf(entity.getReferenceStatus()))
                .priority(entity.getPriority())
                .build();
    }

    private static StorageOwnerType ownerTypeFrom(String value) {
        return StringUtils.isBlank(value) ? null : StorageOwnerType.from(value);
    }

    private static StoredObjectStatus objectStatusFrom(String value) {
        return StringUtils.isBlank(value) ? null : StoredObjectStatus.from(value);
    }

    private static StoredObjectReferenceStatus referenceStatusFrom(String value) {
        return StringUtils.isBlank(value) ? null : StoredObjectReferenceStatus.from(value);
    }

    private static SortDirection sortDirectionFrom(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return SortDirection.valueOf(value.trim().toUpperCase());
    }

    private static String emptyToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private static String valueOf(StorageOwnerType value) {
        return value == null ? null : value.value();
    }

    private static String valueOf(StoredObjectStatus value) {
        return value == null ? null : value.value();
    }

    private static String valueOf(StoredObjectReferenceStatus value) {
        return value == null ? null : value.value();
    }
}
