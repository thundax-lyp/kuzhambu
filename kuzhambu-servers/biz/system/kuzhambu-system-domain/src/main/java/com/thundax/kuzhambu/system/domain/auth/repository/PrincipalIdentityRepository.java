package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalIdentityRepository {

    PrincipalIdentity getById(PrincipalIdentityId id);

    PrincipalIdentity getByIdentity(PrincipalIdentityType identityType, String identityValue);

    PrincipalIdentity getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalIdentityType identityType);

    List<PrincipalIdentity> listByPrincipalKeyAndStatus(PrincipalKey principalKey, PrincipalIdentityStatus status);

    PrincipalIdentityId insert(PrincipalIdentity principalIdentity);

    int update(PrincipalIdentity principalIdentity);

    int updateStatus(PrincipalIdentity principalIdentity);
}
