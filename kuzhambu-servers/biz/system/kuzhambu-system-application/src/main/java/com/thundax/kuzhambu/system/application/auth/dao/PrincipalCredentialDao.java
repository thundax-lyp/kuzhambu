package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.system.application.auth.entity.PrincipalCredential;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalCredentialDao {

    PrincipalCredential getById(PrincipalCredentialId id);

    PrincipalCredential getByIdentityIdAndType(PrincipalIdentityId identityId, PrincipalCredentialType credentialType);

    PrincipalCredential getByPrincipalKeyAndType(PrincipalKey principalKey, PrincipalCredentialType credentialType);

    List<PrincipalCredential> listByPrincipalKeyAndStatus(PrincipalKey principalKey, PrincipalCredentialStatus status);

    PrincipalCredentialId insert(PrincipalCredential principalCredential);

    int update(PrincipalCredential principalCredential);

    int updateStatus(PrincipalCredential principalCredential);

    int updateVerifyState(PrincipalCredential principalCredential);
}
