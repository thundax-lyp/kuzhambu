package com.thundax.kuzhambu.system.application.core.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class LogIdCodec {

    private LogIdCodec() {}

    public static LogId toDomain(Long value) {
        return LogId.ofNullable(value);
    }

    public static Long toValue(LogId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(LogId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<LogId> toDomains(List<Long> values) {
        return values == null ? null : values.stream().map(LogIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<LogId> ids) {
        return ids == null ? null : ids.stream().map(LogIdCodec::toValue).collect(Collectors.toList());
    }
}
