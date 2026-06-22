package com.thundax.kuzhambu.knowledge.domain.taxonomy.codec;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import java.util.List;
import java.util.stream.Collectors;

public final class TagCategoryIdCodec {

    private TagCategoryIdCodec() {}

    public static TagCategoryId toDomain(Long value) {
        return TagCategoryId.ofNullable(value);
    }

    public static TagCategoryId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(TagCategoryId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(TagCategoryId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<TagCategoryId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(TagCategoryIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<TagCategoryId> ids) {
        return ids == null
                ? null
                : ids.stream().map(TagCategoryIdCodec::toValue).collect(Collectors.toList());
    }
}
