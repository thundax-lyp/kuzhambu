package com.thundax.kuzhambu.storage.domain.object.codec;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.util.List;
import java.util.stream.Collectors;

public final class StoredObjectIdCodec {

    private StoredObjectIdCodec() {}

    public static StoredObjectId toDomain(Long value) {
        return StoredObjectId.ofNullable(value);
    }

    public static StoredObjectId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(StoredObjectId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(StoredObjectId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<StoredObjectId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(StoredObjectIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<StoredObjectId> ids) {
        return ids == null
                ? null
                : ids.stream().map(StoredObjectIdCodec::toValue).collect(Collectors.toList());
    }
}
