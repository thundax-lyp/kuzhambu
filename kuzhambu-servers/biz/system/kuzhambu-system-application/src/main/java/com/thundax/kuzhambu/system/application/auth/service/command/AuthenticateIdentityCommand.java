package com.thundax.kuzhambu.system.application.auth.service.command;

import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateIdentityCommand {
    private PrincipalIdentityType identityType;
    private String identityValue;
}
