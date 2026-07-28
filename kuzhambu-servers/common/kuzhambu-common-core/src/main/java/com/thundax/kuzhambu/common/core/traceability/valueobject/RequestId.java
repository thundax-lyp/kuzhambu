package com.thundax.kuzhambu.common.core.traceability.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class RequestId extends BaseStringId {

    public RequestId(String value) {
        super(value == null ? null : value.trim());
    }
}
