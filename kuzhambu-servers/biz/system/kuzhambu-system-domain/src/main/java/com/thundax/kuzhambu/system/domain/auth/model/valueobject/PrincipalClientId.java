package com.thundax.kuzhambu.system.domain.auth.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalClientId extends BaseStringId {

    public PrincipalClientId(String value) {
        super(value == null ? null : value.trim());
    }
}
