package com.thundax.kuzhambu.storage.application.service.command;

import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStorageCommand {
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
