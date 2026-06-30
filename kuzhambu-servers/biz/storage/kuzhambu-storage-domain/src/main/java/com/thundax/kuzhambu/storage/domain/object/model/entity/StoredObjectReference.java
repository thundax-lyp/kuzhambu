package com.thundax.kuzhambu.storage.domain.object.model.entity;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
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

    private String referenceOwnerId;
    private String referenceOwnerType;
    private String ownerParams;
}
