package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class StorageUploadFacadeAssembler {

    public StorageOwnerType toOwnerType(UploadStorageFacadeRequest request) {
        if (request == null || StringUtils.isBlank(request.getOwnerType())) {
            return null;
        }
        return StorageOwnerType.from(request.getOwnerType());
    }

    public StoredObjectStatus toObjectStatus(UploadStorageFacadeRequest request) {
        if (request == null || StringUtils.isBlank(request.getObjectStatus())) {
            return null;
        }
        return StoredObjectStatus.from(request.getObjectStatus());
    }

    public StoredObjectReferenceStatus toReferenceStatus(UploadStorageFacadeRequest request) {
        if (request == null || StringUtils.isBlank(request.getReferenceStatus())) {
            return null;
        }
        return StoredObjectReferenceStatus.from(request.getReferenceStatus());
    }

    public UploadStorageFacadeResponse toResponse(StoredObject storage) {
        if (storage == null) {
            return null;
        }
        return UploadStorageFacadeResponse.builder()
                .storageObjectId(
                        storage.getId() == null ? null : storage.getId().value())
                .originalFilename(storage.getOriginalFilename())
                .contentType(storage.getContentType())
                .name(storage.getName())
                .extendName(storage.getExtendName())
                .mimeType(storage.getMimeType())
                .bucketName(storage.getBucketName())
                .objectKey(storage.getObjectKey())
                .sizeBytes(storage.getSize())
                .accessEndpoint(storage.getAccessEndpoint())
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
