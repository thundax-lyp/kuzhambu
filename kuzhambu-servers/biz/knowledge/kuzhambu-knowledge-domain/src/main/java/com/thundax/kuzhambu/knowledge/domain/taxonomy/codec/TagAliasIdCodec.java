package com.thundax.kuzhambu.knowledge.domain.taxonomy.codec;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import java.util.List;
import java.util.stream.Collectors;

public final class TagAliasIdCodec {

    private TagAliasIdCodec() {}

    public static TagAliasId toDomain(Long value) {
        return TagAliasId.ofNullable(value);
    }

    public static TagAliasId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(TagAliasId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(TagAliasId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<TagAliasId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(TagAliasIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<TagAliasId> ids) {
        return ids == null ? null : ids.stream().map(TagAliasIdCodec::toValue).collect(Collectors.toList());
    }
}
