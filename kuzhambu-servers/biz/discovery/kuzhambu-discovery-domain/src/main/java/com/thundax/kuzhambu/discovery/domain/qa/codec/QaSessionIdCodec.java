package com.thundax.kuzhambu.discovery.domain.qa.codec;

import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import java.util.List;
import java.util.stream.Collectors;

public final class QaSessionIdCodec {

    private QaSessionIdCodec() {}

    public static QaSessionId toDomain(Long value) {
        return value == null ? null : new QaSessionId(value);
    }

    public static QaSessionId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(QaSessionId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(QaSessionId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<QaSessionId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(QaSessionIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<QaSessionId> ids) {
        return ids == null ? null : ids.stream().map(QaSessionIdCodec::toValue).collect(Collectors.toList());
    }
}
