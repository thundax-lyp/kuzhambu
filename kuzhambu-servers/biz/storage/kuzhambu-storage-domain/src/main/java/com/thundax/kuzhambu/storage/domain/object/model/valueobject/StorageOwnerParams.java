package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import java.io.Serializable;
import java.util.Objects;

public final class StorageOwnerParams implements Serializable {

    private final String value;

    public StorageOwnerParams(String value) {
        this.value = Objects.requireNonNull(value, "storage owner params must not be null")
                .trim();
    }

    public String value() {
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
        StorageOwnerParams that = (StorageOwnerParams) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
