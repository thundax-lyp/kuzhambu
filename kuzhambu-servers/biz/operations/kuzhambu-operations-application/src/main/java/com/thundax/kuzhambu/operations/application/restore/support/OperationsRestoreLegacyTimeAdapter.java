package com.thundax.kuzhambu.operations.application.restore.support;

import java.time.Instant;
import java.util.Date;

public final class OperationsRestoreLegacyTimeAdapter {

    private OperationsRestoreLegacyTimeAdapter() {}

    public static Date toDate(Instant instant) {
        return instant == null ? null : Date.from(instant);
    }
}
