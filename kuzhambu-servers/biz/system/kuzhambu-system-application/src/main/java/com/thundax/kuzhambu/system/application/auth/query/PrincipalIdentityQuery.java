package com.thundax.kuzhambu.system.application.auth.query;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;

public record PrincipalIdentityQuery(
        PrincipalIdentityId id,
        PrincipalIdentityType identityType,
        String identityValue,
        PrincipalKey principalKey,
        PrincipalIdentityStatus status) {}
