package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalClientId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import java.util.List;

public interface PrincipalAccessTokenRepository {

    PrincipalAccessToken getById(PrincipalAccessTokenId id);

    PrincipalAccessToken getByTokenCode(PrincipalAccessTokenCode tokenCode);

    PrincipalAccessToken getByToken(String token);

    List<PrincipalAccessToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, PrincipalClientId clientId, PrincipalTokenStatus status);

    int countByClientIdAndStatus(PrincipalClientId clientId, PrincipalTokenStatus status);

    PrincipalAccessTokenId insert(PrincipalAccessToken accessToken, String token);

    int updateStatus(PrincipalAccessToken accessToken);
}
