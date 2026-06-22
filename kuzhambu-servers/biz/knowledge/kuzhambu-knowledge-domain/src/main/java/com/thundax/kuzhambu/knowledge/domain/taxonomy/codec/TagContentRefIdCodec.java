package com.thundax.kuzhambu.knowledge.domain.taxonomy.codec;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import java.util.List;
import java.util.stream.Collectors;

public final class TagContentRefIdCodec {

    private TagContentRefIdCodec() {}

    public static TagContentRefId toDomain(Long value) {
        return TagContentRefId.ofNullable(value);
    }

    public static TagContentRefId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(TagContentRefId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(TagContentRefId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<TagContentRefId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(TagContentRefIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<TagContentRefId> ids) {
        return ids == null ? null : ids.stream().map(TagContentRefIdCodec::toValue).collect(Collectors.toList());
    }
}
