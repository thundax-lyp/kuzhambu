package com.thundax.kuzhambu.system.application.auth.service.query;

import com.thundax.kuzhambu.system.domain.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.model.enums.PrincipalIdentityType;
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
public class PrincipalIdentityQuery {
    private PrincipalIdentityId id;
    private PrincipalIdentityType identityType;
    private String identityValue;
    private PrincipalKey principalKey;
    private PrincipalIdentityStatus status;
}
