package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;

public record CreatePrincipalIdentityCommand(
        PrincipalKey principalKey, PrincipalIdentityType type, String identityValue, PrincipalIdentityStatus status) {}
