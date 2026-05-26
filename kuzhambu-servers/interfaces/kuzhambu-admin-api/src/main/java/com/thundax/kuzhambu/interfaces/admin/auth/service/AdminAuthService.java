package com.thundax.kuzhambu.interfaces.admin.auth.service;

import com.thundax.kuzhambu.biz.core.entity.User;
import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.interfaces.admin.auth.service.command.AdminAuthCommand;
import com.thundax.kuzhambu.interfaces.admin.auth.service.query.AdminAuthQuery;
import com.thundax.kuzhambu.interfaces.admin.auth.service.result.AuthAccessTokenResult;
import com.thundax.kuzhambu.interfaces.admin.auth.service.result.AuthTokenQueryResult;
import com.thundax.kuzhambu.interfaces.admin.auth.service.result.AuthTokenRefreshResult;
import com.thundax.kuzhambu.interfaces.admin.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.thundax.kuzhambu.interfaces.admin.auth.service.result.OAuth2AuthorizationViewResult;
import org.springframework.lang.NonNull;

public interface AdminAuthService {

    @NonNull
    AuthAccessTokenResult createAccessToken(AdminAuthCommand command);

    AuthAccessTokenResult getAccessToken(AdminAuthQuery query);

    int deleteAccessTokensByUserId(AdminAuthCommand command);

    boolean validateToken(AdminAuthCommand command);

    void activeAccessToken(AdminAuthCommand command);

    void deleteAccessToken(AdminAuthCommand command);

    AuthTokenQueryResult getTokenInfo(AdminAuthQuery query);

    AuthTokenRefreshResult refreshAccessToken(AdminAuthCommand command);

    OAuth2AuthorizationViewResult authorizeOAuth2(AdminAuthCommand command);

    OAuth2AuthorizationDecisionResult decideOAuth2(AdminAuthCommand command);

    AuthTokenRefreshResult exchangeOAuth2Token(AdminAuthCommand command);

    @LayerPublicApi(reason = "OAuth2 授权码吊销的后台认证业务入口")
    boolean revokeAuthorizationCode(AdminAuthCommand command);

    boolean revokeOAuth2Token(AdminAuthCommand command);

    void invalidateSessionByToken(AdminAuthCommand command);

    @LayerPublicApi(reason = "账号状态变化时按用户维度失效在线会话的业务入口")
    int invalidateSessionsByUserId(AdminAuthCommand command);

    User authenticatePassword(AdminAuthCommand command);

    User authenticateSms(AdminAuthCommand command);

    User authenticateWecom(AdminAuthCommand command);

    User authenticateGithub(AdminAuthCommand command);

    void recordLoginFailed(AdminAuthCommand command);

    void validatePassword(AdminAuthCommand command);
}
