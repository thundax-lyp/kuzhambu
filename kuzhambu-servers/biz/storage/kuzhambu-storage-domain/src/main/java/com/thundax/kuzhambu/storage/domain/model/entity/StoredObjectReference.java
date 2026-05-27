package com.thundax.kuzhambu.storage.domain.model.entity;

import com.thundax.kuzhambu.storage.domain.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.model.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectReference {
    private StoredObjectId objectId;

    private String ownerId;
    private StorageOwnerType ownerType;
    private String ownerParams;
    private StoredObjectReferenceStatus referenceStatus;
}
