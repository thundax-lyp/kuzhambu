package com.thundax.kuzhambu.operations.domain.health.codec;

import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;

public final class HealthCheckIdCodec {

    private HealthCheckIdCodec() {}

    public static HealthCheckId toDomain(Long value) {
        return value == null ? null : new HealthCheckId(value);
    }

    public static Long toValue(HealthCheckId id) {
        return id == null ? null : id.value();
    }
}
