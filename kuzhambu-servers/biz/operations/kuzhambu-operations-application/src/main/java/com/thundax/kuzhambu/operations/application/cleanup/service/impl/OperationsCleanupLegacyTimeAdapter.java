package com.thundax.kuzhambu.operations.application.cleanup.service.impl;

import java.time.Instant;
import java.util.Date;

final class OperationsCleanupLegacyTimeAdapter {

    private OperationsCleanupLegacyTimeAdapter() {}

    static Date toDate(Instant instant) {
        return instant == null ? null : Date.from(instant);
    }
}
