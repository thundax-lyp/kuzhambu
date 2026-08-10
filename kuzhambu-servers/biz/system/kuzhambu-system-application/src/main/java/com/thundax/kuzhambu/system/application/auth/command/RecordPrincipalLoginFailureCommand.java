package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;

public record RecordPrincipalLoginFailureCommand(
        PrincipalKey principalKey,
        PrincipalAuthenticationMethod authenticationMethod,
        PrincipalIdentityType identityType,
        String ip,
        String userAgent,
        String reason) {}
