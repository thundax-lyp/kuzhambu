package com.thundax.kuzhambu.system.domain.auth.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalAuthSessionId extends BaseStringId {

    public PrincipalAuthSessionId(String value) {
        super(value == null ? null : value.trim());
    }
}
