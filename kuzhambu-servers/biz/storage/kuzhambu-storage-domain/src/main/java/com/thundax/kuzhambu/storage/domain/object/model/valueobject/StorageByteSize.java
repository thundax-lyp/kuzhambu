package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import java.io.Serializable;
import java.util.Objects;

public final class StorageByteSize implements Serializable {

    private final Long value;

    public StorageByteSize(Long value) {
        this.value = Objects.requireNonNull(value, "storage byte size must not be null");
        if (value < 0) {
            throw new IllegalArgumentException("storage byte size must be greater than or equal to 0");
        }
    }

    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        StorageByteSize that = (StorageByteSize) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
