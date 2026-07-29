package com.thundax.kuzhambu.system.application.auth.service;

import com.thundax.kuzhambu.system.application.auth.command.CreateAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.query.AdminAccessTokenQuery;
import com.thundax.kuzhambu.system.application.auth.result.AdminAccessTokenResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenQueryResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenRefreshResult;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public interface AdminTokenApplicationService {

    AdminAccessTokenResult createAccessToken(CreateAdminAccessTokenCommand command);

    AdminAccessTokenResult getAccessToken(AdminAccessTokenQuery query);

    int deleteAccessTokensByUserId(UserId userId);

    boolean validateToken(AdminAccessTokenQuery query);

    void activeAccessToken(AdminAccessTokenQuery query);

    void deleteAccessToken(String token, String ip, String userAgent);

    AdminTokenQueryResult getTokenInfo(AdminAccessTokenQuery query);

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
