package com.thundax.kuzhambu.system.domain.audit.codec;

import com.thundax.kuzhambu.system.domain.model.valueobject.AuditMetaId;
import java.util.List;
import java.util.stream.Collectors;

public final class AuditMetaIdCodec {

    private AuditMetaIdCodec() {}

    public static AuditMetaId toDomain(Long value) {
        return AuditMetaId.ofNullable(value);
    }

    public static Long toValue(AuditMetaId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(AuditMetaId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<AuditMetaId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(AuditMetaIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<AuditMetaId> ids) {
        return ids == null ? null : ids.stream().map(AuditMetaIdCodec::toValue).collect(Collectors.toList());
    }
}
