package com.thundax.kuzhambu.system.domain.auth.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PreAuthSessionId extends BaseStringId {

    public PreAuthSessionId(String value) {
        super(value == null ? null : value.trim());
    }
}
