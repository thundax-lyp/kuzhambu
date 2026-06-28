package com.thundax.kuzhambu.storage.application.facade.assembler;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class StorageUploadFacadeAssembler {

    public StorageOwnerType toOwnerType(UploadStorageObjectFacadeRequest request) {
        if (request == null || StringUtils.isBlank(request.getOwnerType())) {
            return null;
        }
        return StorageOwnerType.from(request.getOwnerType());
    }

    public UploadStorageObjectFacadeResponse toResponse(StoredObject storage) {
        if (storage == null) {
            return null;
        }
        return UploadStorageObjectFacadeResponse.builder()
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
