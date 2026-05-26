package com.thundax.kuzhambu.biz.auth.dao;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalCredential;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalCredentialStatus;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalCredentialId;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalIdentityId;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalKey;
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
