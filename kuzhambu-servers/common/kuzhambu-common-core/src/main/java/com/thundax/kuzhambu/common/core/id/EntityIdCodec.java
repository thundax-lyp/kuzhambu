package com.thundax.kuzhambu.common.core.id;

import java.util.List;
import java.util.stream.Collectors;

public final class EntityIdCodec {

    private EntityIdCodec() {}

    public static EntityId toDomain(Long value) {
        return EntityId.ofNullable(value);
    }

    public static Long toValue(EntityId entityId) {
        return entityId == null ? null : entityId.value();
    }

    public static String toStringValue(EntityId entityId) {
        return entityId == null ? null : String.valueOf(entityId.value());
    }

    public static List<EntityId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(EntityIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<EntityId> entityIds) {
        return entityIds == null
                ? null
                : entityIds.stream().map(EntityIdCodec::toValue).collect(Collectors.toList());
    }
}
