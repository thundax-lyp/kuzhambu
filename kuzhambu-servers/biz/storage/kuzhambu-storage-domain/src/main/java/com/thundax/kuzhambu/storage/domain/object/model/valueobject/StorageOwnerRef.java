package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import java.util.Objects;

public final class StorageOwnerRef {

    private final StorageOwnerType ownerType;
    private final String ownerId;

    private StorageOwnerRef(StorageOwnerType ownerType, String ownerId) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
    }

    public static StorageOwnerRef of(StorageOwnerType ownerType, String ownerId) {
        if (ownerType == null) {
            throw new IllegalArgumentException("owner type must not be null");
        }
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new IllegalArgumentException("owner id must not be blank");
        }
        return new StorageOwnerRef(ownerType, ownerId.trim());
    }

    public static StorageOwnerRef ofNullable(StorageOwnerType ownerType, String ownerId) {
        if (ownerType == null && (ownerId == null || ownerId.trim().isEmpty())) {
            return null;
        }
        return new StorageOwnerRef(ownerType, ownerId == null ? null : ownerId.trim());
    }

    public StorageOwnerType ownerType() {
        return ownerType;
    }

    public String ownerId() {
        return ownerId;
    }

    public String ownerTypeValue() {
        return ownerType == null ? null : ownerType.value();
    }

    public StorageOwnerRef withOwnerType(StorageOwnerType value) {
        return ofNullable(value, ownerId);
    }

    public StorageOwnerRef withOwnerId(String value) {
        return ofNullable(ownerType, value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        StorageOwnerRef that = (StorageOwnerRef) other;
        return ownerType == that.ownerType && Objects.equals(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerType, ownerId);
    }
}
