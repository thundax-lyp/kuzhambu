package com.thundax.kuzhambu.common.core.traceability.codec;

import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import java.util.UUID;

public final class TraceIdCodec {

    private TraceIdCodec() {}

    public static TraceId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new TraceId(value);
    }

    public static String toValue(TraceId traceId) {
        return traceId == null ? null : traceId.value();
    }

    public static TraceId generate() {
        return toDomain(UUID.randomUUID().toString());
    }
}
