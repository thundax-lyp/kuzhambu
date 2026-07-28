package com.thundax.kuzhambu.knowledge.domain.taxonomy.codec;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.List;
import java.util.stream.Collectors;

public final class TagIdCodec {

    private TagIdCodec() {}

    public static TagId toDomain(Long value) {
        return value == null ? null : new TagId(value);
    }

    public static TagId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(TagId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(TagId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<TagId> toDomains(List<Long> values) {
        return values == null ? null : values.stream().map(TagIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<TagId> ids) {
        return ids == null ? null : ids.stream().map(TagIdCodec::toValue).collect(Collectors.toList());
    }
}
