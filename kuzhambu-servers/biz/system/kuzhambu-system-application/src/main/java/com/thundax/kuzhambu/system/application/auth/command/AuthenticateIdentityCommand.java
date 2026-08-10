package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;

public record AuthenticateIdentityCommand(PrincipalIdentityType identityType, String identityValue) {}
