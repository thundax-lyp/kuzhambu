package com.thundax.kuzhambu.system.domain.core.codec;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import java.util.List;
import java.util.stream.Collectors;

public final class MenuIdCodec {

    private MenuIdCodec() {}

    public static MenuId toDomain(Long value) {
        return MenuId.ofNullable(value);
    }

    public static MenuId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(MenuId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(MenuId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<MenuId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(MenuIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<MenuId> ids) {
        return ids == null ? null : ids.stream().map(MenuIdCodec::toValue).collect(Collectors.toList());
    }
}
