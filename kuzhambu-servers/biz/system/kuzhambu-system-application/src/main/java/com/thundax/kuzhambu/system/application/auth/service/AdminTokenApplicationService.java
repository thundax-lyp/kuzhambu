package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.system.application.auth.result.AdminAccessTokenResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenQueryResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenRefreshResult;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public interface AdminTokenApplicationService {

    AdminAccessTokenResult createAccessToken(
            UserId userId,
            String loginName,
            String ip,
            String userAgent,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType);

    AdminAccessTokenResult getAccessToken(String token);

    int deleteAccessTokensByUserId(UserId userId);

    boolean validateToken(String token);

    void activeAccessToken(String token);

    void deleteAccessToken(String token, String ip, String userAgent);

    AdminTokenQueryResult getTokenInfo(String token);

    AdminTokenRefreshResult refreshAccessToken(String clientId, String refreshToken, String ip, String userAgent);

    void invalidateSessionByToken(String token, String reason);

    int invalidateSessionsByUserId(UserId userId, String reason);

    void recordLoginFailed(
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason);

    void recordLoginFailed(
            PrincipalKey principalKey,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason);
}
