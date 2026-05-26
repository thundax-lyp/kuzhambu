package com.thundax.kuzhambu.biz.auth.dao;

import com.thundax.kuzhambu.biz.auth.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.biz.auth.entity.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalAccessTokenId;
import com.thundax.kuzhambu.biz.auth.entity.valueobject.PrincipalKey;
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
