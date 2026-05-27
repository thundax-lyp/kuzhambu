package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.system.domain.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalKey;
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
