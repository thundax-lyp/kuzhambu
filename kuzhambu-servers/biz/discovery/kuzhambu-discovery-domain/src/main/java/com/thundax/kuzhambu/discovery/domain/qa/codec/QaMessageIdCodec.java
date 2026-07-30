package com.thundax.kuzhambu.discovery.domain.qa.codec;

import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageId;
import java.util.List;
import java.util.stream.Collectors;

public final class QaMessageIdCodec {

    private QaMessageIdCodec() {}

    public static QaMessageId toDomain(Long value) {
        return value == null ? null : new QaMessageId(value);
    }

    public static QaMessageId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(QaMessageId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(QaMessageId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<QaMessageId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(QaMessageIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<QaMessageId> ids) {
        return ids == null ? null : ids.stream().map(QaMessageIdCodec::toValue).collect(Collectors.toList());
    }
}
