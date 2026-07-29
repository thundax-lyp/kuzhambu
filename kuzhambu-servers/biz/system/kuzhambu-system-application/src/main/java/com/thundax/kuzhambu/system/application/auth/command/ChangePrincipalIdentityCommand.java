package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePrincipalIdentityCommand {
    private PrincipalIdentityId id;
    private PrincipalKey principalKey;
    private PrincipalIdentityType type;
    private String identityValue;
    private PrincipalIdentityStatus status;
}
