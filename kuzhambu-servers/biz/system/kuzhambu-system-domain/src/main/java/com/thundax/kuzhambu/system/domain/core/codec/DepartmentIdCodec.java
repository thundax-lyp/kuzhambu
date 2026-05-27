package com.thundax.kuzhambu.system.domain.core.codec;

import com.thundax.kuzhambu.system.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.stream.Collectors;

public final class DepartmentIdCodec {

    private DepartmentIdCodec() {}

    public static DepartmentId toDomain(Long value) {
        return DepartmentId.ofNullable(value);
    }

    public static DepartmentId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(DepartmentId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(DepartmentId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<DepartmentId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(DepartmentIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<DepartmentId> ids) {
        return ids == null ? null : ids.stream().map(DepartmentIdCodec::toValue).collect(Collectors.toList());
    }
}
