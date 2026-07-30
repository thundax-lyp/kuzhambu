package com.thundax.kuzhambu.discovery.domain.search.codec;

import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.QueryUnderstandingId;

public final class QueryUnderstandingIdCodec {

    private QueryUnderstandingIdCodec() {}

    public static QueryUnderstandingId toDomain(Long value) {
        return value == null ? null : new QueryUnderstandingId(value);
    }

    public static QueryUnderstandingId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(QueryUnderstandingId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(QueryUnderstandingId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
