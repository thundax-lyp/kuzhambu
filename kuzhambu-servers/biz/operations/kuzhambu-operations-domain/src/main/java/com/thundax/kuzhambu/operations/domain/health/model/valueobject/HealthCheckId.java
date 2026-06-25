package com.thundax.kuzhambu.operations.domain.health.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class HealthCheckId extends BaseLongId {

    private HealthCheckId(Long value) {
        super(value);
    }

    public static HealthCheckId of(Long value) {
        return new HealthCheckId(value);
    }

    public static HealthCheckId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
