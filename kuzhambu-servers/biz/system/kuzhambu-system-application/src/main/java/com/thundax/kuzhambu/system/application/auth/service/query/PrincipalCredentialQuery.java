package com.thundax.kuzhambu.system.application.auth.service.query;

import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalCredentialQuery {
    private PrincipalCredentialId id;
    private PrincipalIdentityId identityId;
    private PrincipalCredentialType credentialType;
    private PrincipalKey principalKey;
    private PrincipalCredentialStatus status;
}
