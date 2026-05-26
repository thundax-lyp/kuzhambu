package com.thundax.kuzhambu.biz.core.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class RoleIdCodec {

    private RoleIdCodec() {}

    public static RoleId toDomain(Long value) {
        return RoleId.ofNullable(value);
    }

    public static RoleId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(RoleId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(RoleId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<RoleId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(RoleIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<RoleId> ids) {
        return ids == null ? null : ids.stream().map(RoleIdCodec::toValue).collect(Collectors.toList());
    }
}
