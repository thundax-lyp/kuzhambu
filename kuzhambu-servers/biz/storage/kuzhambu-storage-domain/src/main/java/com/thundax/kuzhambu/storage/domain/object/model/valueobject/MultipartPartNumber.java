package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import java.io.Serializable;
import java.util.Objects;

public final class MultipartPartNumber implements Serializable {

    private final Integer value;

    public MultipartPartNumber(Integer value) {
        this.value = Objects.requireNonNull(value, "multipart part number must not be null");
        if (value < 1) {
            throw new IllegalArgumentException("multipart part number must be greater than or equal to 1");
        }
    }

    public Integer value() {
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
        MultipartPartNumber that = (MultipartPartNumber) other;
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
