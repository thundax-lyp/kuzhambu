package com.thundax.kuzhambu.storage.domain.object.model.entity;

import com.thundax.kuzhambu.storage.domain.object.codec.StorageOwnerParamsCodec;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerParams;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
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

    private StorageOwnerRef referenceOwnerRef;
    private StorageOwnerParams ownerParams;

    public StoredObjectReference(
            StoredObjectId objectId, String referenceOwnerId, String referenceOwnerType, String ownerParams) {
        this.objectId = objectId;
        this.referenceOwnerRef = StorageOwnerRef.ofNullable(ownerTypeFrom(referenceOwnerType), referenceOwnerId);
        setOwnerParams(ownerParams);
    }

    public String getOwnerParams() {
        return StorageOwnerParamsCodec.toValue(ownerParams);
    }

    public void setOwnerParams(String ownerParams) {
        this.ownerParams = StorageOwnerParamsCodec.toDomain(ownerParams);
    }

    public StorageOwnerParams getOwnerParamsRef() {
        return ownerParams;
    }

    public void setOwnerParamsRef(StorageOwnerParams ownerParams) {
        this.ownerParams = ownerParams;
    }

    public String getReferenceOwnerId() {
        return referenceOwnerRef == null ? null : referenceOwnerRef.ownerId();
    }

    public void setReferenceOwnerId(String referenceOwnerId) {
        this.referenceOwnerRef = referenceOwnerRef == null
                ? StorageOwnerRef.ofNullable(null, referenceOwnerId)
                : referenceOwnerRef.withOwnerId(referenceOwnerId);
    }

    public String getReferenceOwnerType() {
        return referenceOwnerRef == null ? null : referenceOwnerRef.ownerTypeValue();
    }

    public void setReferenceOwnerType(String referenceOwnerType) {
        this.referenceOwnerRef = referenceOwnerRef == null
                ? StorageOwnerRef.ofNullable(ownerTypeFrom(referenceOwnerType), null)
                : referenceOwnerRef.withOwnerType(ownerTypeFrom(referenceOwnerType));
    }

    private static StorageOwnerType ownerTypeFrom(String value) {
        return value == null || value.trim().isEmpty() ? null : StorageOwnerType.from(value);
    }
}
