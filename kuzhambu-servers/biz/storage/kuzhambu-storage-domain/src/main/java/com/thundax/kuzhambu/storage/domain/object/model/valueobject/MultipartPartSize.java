package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import java.io.Serializable;
import java.util.Objects;

public final class MultipartPartSize implements Serializable {

    private final Long value;

    public MultipartPartSize(Long value) {
        this.value = Objects.requireNonNull(value, "multipart part size must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("multipart part size must be greater than 0");
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
        MultipartPartSize that = (MultipartPartSize) other;
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
