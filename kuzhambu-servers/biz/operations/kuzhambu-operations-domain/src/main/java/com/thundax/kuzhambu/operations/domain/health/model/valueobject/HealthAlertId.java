package com.thundax.kuzhambu.operations.domain.health.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseLongId;

public final class HealthAlertId extends BaseLongId {

    private HealthAlertId(Long value) {
        super(value);
    }

    public static HealthAlertId of(Long value) {
        return new HealthAlertId(value);
    }

    public static HealthAlertId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
