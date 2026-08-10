package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public record CreateAdminAccessTokenCommand(
        UserId userId,
        String loginName,
        String ip,
        String userAgent,
        PrincipalAuthenticationMethod authenticationMethod,
        PrincipalIdentityType identityType) {}
