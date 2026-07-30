package com.thundax.kuzhambu.discovery.domain.search.codec;

import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchClickEventId;

public final class SearchClickEventIdCodec {

    private SearchClickEventIdCodec() {}

    public static SearchClickEventId toDomain(Long value) {
        return value == null ? null : new SearchClickEventId(value);
    }

    public static SearchClickEventId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(SearchClickEventId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(SearchClickEventId id) {
        return id == null ? null : String.valueOf(id.value());
    }
}
