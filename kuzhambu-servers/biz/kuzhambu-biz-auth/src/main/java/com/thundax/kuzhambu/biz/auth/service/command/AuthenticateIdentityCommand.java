package com.thundax.kuzhambu.biz.auth.service.command;

import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityType;
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
