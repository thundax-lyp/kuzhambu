package com.thundax.kuzhambu.knowledge.domain.taxonomy.codec;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.SynonymId;
import java.util.List;
import java.util.stream.Collectors;

public final class SynonymIdCodec {

    private SynonymIdCodec() {}

    public static SynonymId toDomain(Long value) {
        return SynonymId.ofNullable(value);
    }

    public static SynonymId toDomain(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return toDomain(Long.valueOf(value.trim()));
    }

    public static Long toValue(SynonymId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(SynonymId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<SynonymId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(SynonymIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<SynonymId> ids) {
        return ids == null ? null : ids.stream().map(SynonymIdCodec::toValue).collect(Collectors.toList());
    }
}
