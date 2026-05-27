package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalKey;
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
