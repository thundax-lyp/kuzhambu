package com.thundax.kuzhambu.system.domain.core.codec;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.util.List;
import java.util.stream.Collectors;

public final class UserIdCodec {

    private UserIdCodec() {}

    public static UserId toDomain(Long value) {
        return UserId.ofNullable(value);
    }

    public static UserId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(UserId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(UserId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<UserId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(UserIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<UserId> ids) {
        return ids == null ? null : ids.stream().map(UserIdCodec::toValue).collect(Collectors.toList());
    }
}
