package com.thundax.kuzhambu.operations.application.dashboard.support;

import java.time.Instant;
import java.util.Date;

public final class OperationsDashboardLegacyTimeAdapter {

    private OperationsDashboardLegacyTimeAdapter() {}

    static Date toDate(Instant instant) {
        return instant == null ? null : Date.from(instant);
    }

    public static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
