package com.thundax.kuzhambu.discovery.domain.search.codec;

import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import java.util.List;
import java.util.stream.Collectors;

public final class SearchEventIdCodec {

    private SearchEventIdCodec() {}

    public static SearchEventId toDomain(Long value) {
        return value == null ? null : new SearchEventId(value);
    }

    public static SearchEventId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(SearchEventId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(SearchEventId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<SearchEventId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(SearchEventIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<SearchEventId> ids) {
        return ids == null
                ? null
                : ids.stream().map(SearchEventIdCodec::toValue).collect(Collectors.toList());
    }
}
