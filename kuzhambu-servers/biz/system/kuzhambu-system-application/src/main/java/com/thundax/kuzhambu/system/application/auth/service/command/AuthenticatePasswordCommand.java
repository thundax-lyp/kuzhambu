package com.thundax.kuzhambu.system.application.auth.service.command;

import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatePasswordCommand {
    private PrincipalIdentityType identityType;
    private String identityValue;
    private PrincipalCredentialType credentialType;
    private String plainPassword;
    private PrincipalPasswordPolicyDTO passwordPolicy;
}
