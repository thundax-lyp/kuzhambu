package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalCredentialRepository {

    PrincipalCredential getById(PrincipalCredentialId id);

    PrincipalCredential getByIdentityIdAndType(PrincipalIdentityId identityId, PrincipalCredentialType credentialType);

    PrincipalCredential getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalCredentialType credentialType);

    List<PrincipalCredential> listByPrincipalKeyAndStatus(PrincipalKey principalKey, PrincipalCredentialStatus status);

    PrincipalCredentialId insert(PrincipalCredential principalCredential);

    int update(PrincipalCredential principalCredential);

    int updateStatus(PrincipalCredential principalCredential);

    int updateVerifyState(PrincipalCredential principalCredential);
}
