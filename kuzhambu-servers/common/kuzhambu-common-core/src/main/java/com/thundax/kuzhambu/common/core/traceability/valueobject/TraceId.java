package com.thundax.kuzhambu.common.core.traceability.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class TraceId extends BaseStringId {

    public TraceId(String value) {
        super(value == null ? null : value.trim());
    }
}
