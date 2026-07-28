package com.thundax.kuzhambu.system.domain.auth.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class PrincipalRefreshTokenId extends BaseStringId {

    public PrincipalRefreshTokenId(String value) {
        super(value == null ? null : value.trim());
    }
}
