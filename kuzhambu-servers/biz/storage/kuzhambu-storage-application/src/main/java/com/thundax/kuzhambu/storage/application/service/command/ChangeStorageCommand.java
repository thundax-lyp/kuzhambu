package com.thundax.kuzhambu.storage.application.service.command;

import com.thundax.kuzhambu.storage.application.entity.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.application.entity.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.application.entity.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.model.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStorageCommand {
    private StoredObjectId id;
    private String originalFilename;
    private String contentType;
    private String name;
    private String extendName;
    private String mimeType;
    private String ownerId;
    private StorageOwnerType ownerType;
    private String bucketName;
    private String objectKey;
    private Long size;
    private String accessEndpoint;
    private StoredObjectStatus objectStatus;
    private StoredObjectReferenceStatus referenceStatus;
    private String remarks;
}
