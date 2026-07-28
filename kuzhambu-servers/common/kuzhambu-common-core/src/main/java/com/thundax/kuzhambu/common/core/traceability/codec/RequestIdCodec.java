package com.thundax.kuzhambu.common.core.traceability.codec;

import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import java.util.UUID;

public final class RequestIdCodec {

    private RequestIdCodec() {}

    public static RequestId toDomain(String value) {
        return value == null || value.trim().isEmpty() ? null : new RequestId(value);
    }

    public static String toValue(RequestId requestId) {
        return requestId == null ? null : requestId.value();
    }

    public static RequestId generate() {
        return toDomain(UUID.randomUUID().toString());
    }
}
