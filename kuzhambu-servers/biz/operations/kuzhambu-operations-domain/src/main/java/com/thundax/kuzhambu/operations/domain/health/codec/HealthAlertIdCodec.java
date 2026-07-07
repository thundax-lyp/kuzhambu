package com.thundax.kuzhambu.operations.domain.health.codec;

import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;

public final class HealthAlertIdCodec {

    private HealthAlertIdCodec() {}

    public static HealthAlertId toDomain(Long value) {
        return HealthAlertId.ofNullable(value);
    }

    public static Long toValue(HealthAlertId id) {
        return id == null ? null : id.value();
    }
}
