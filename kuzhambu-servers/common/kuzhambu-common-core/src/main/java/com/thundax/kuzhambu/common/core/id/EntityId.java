package com.thundax.kuzhambu.common.core.id;

public final class EntityId extends BaseLongId {

    private EntityId(Long value) {
        super(value);
    }

    public static EntityId of(Long value) {
        return new EntityId(value);
    }

    public static EntityId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
