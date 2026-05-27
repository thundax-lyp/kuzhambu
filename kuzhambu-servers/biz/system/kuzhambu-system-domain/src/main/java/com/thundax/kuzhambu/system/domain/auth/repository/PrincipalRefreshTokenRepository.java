package com.thundax.kuzhambu.system.domain.auth.repository;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalRefreshToken;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenId;
import java.util.List;

public interface PrincipalRefreshTokenRepository {

    PrincipalRefreshToken getById(PrincipalRefreshTokenId id);

    PrincipalRefreshToken getByTokenCode(PrincipalRefreshTokenCode tokenCode);

    PrincipalRefreshToken getByToken(String token);

    List<PrincipalRefreshToken> listByPrincipalKeyAndClientIdAndStatus(
            PrincipalKey principalKey, String clientId, PrincipalTokenStatus status);

    PrincipalRefreshTokenId insert(PrincipalRefreshToken refreshToken, String token);

    int updateStatus(PrincipalRefreshToken refreshToken);
}
