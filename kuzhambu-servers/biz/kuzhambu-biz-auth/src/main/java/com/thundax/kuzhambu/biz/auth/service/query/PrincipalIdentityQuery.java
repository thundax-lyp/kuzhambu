package com.thundax.kuzhambu.biz.auth.service.query;

import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalKey;
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
