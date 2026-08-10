package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;

public record AuthenticatePasswordCommand(
        PrincipalIdentityType identityType,
        String identityValue,
        PrincipalCredentialType credentialType,
        String plainPassword,
        PrincipalPasswordPolicyDTO passwordPolicy) {}
