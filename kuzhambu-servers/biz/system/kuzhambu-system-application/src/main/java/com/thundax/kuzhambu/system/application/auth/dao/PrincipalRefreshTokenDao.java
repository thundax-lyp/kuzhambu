package com.thundax.kuzhambu.system.application.auth.dao;

import com.thundax.kuzhambu.system.domain.model.entity.PrincipalRefreshToken;
import com.thundax.kuzhambu.system.domain.model.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.domain.model.valueobject.PrincipalRefreshTokenId;
import java.util.List;

public interface PrincipalRefreshTokenDao {

    PrincipalRefreshToken getById(PrincipalRefreshTokenId id);

    PrincipalRefreshToken getByTokenCode(PrincipalRefreshTokenCode tokenCode);

    PrincipalRefreshToken getByToken(String token);

    List<PrincipalRefreshToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status);

    PrincipalRefreshTokenId insert(PrincipalRefreshToken refreshToken, String token);

    int updateStatus(PrincipalRefreshToken refreshToken);
}
