package com.thundax.kuzhambu.biz.auth.service.command;

import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.biz.auth.service.dto.PrincipalPasswordPolicyDTO;
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
