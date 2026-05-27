package com.thundax.kuzhambu.system.application.auth.service.query;

import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalKey;
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
