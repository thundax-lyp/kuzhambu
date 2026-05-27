package com.thundax.kuzhambu.system.application.auth.service.query;

import com.thundax.kuzhambu.system.domain.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalKey;
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
