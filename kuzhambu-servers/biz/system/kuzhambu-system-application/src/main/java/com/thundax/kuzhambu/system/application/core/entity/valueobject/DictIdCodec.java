package com.thundax.kuzhambu.system.application.core.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class DictIdCodec {

    private DictIdCodec() {}

    public static DictId toDomain(Long value) {
        return DictId.ofNullable(value);
    }

    public static DictId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(DictId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(DictId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<DictId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(DictIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<DictId> ids) {
        return ids == null ? null : ids.stream().map(DictIdCodec::toValue).collect(Collectors.toList());
    }
}
