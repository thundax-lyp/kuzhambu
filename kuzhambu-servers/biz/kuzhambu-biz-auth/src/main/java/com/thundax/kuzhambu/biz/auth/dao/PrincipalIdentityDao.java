package com.thundax.kuzhambu.biz.auth.dao;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalIdentityDao {

    PrincipalIdentity getById(PrincipalIdentityId id);

    PrincipalIdentity getByIdentity(PrincipalIdentityType identityType, String identityValue);

    PrincipalIdentity getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalIdentityType identityType);

    List<PrincipalIdentity> listByPrincipalKeyAndStatus(PrincipalKey principalKey, PrincipalIdentityStatus status);

    PrincipalIdentityId insert(PrincipalIdentity principalIdentity);

    int update(PrincipalIdentity principalIdentity);

    int updateStatus(PrincipalIdentity principalIdentity);
}
