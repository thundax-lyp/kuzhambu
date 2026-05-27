package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.system.application.auth.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalAccessTokenId;
import com.thundax.kuzhambu.system.domain.auth.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalAccessTokenDao {

    PrincipalAccessToken getById(PrincipalAccessTokenId id);

    PrincipalAccessToken getByTokenCode(PrincipalAccessTokenCode tokenCode);

    PrincipalAccessToken getByToken(String token);

    List<PrincipalAccessToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status);

    int countByClientIdAndStatus(String clientId, PrincipalTokenStatus status);

    PrincipalAccessTokenId insert(PrincipalAccessToken accessToken, String token);

    int updateStatus(PrincipalAccessToken accessToken);
}
