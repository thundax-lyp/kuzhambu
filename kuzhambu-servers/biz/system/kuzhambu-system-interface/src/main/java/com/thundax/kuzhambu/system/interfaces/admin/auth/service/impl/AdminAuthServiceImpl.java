package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import com.thundax.kuzhambu.system.application.auth.configure.AuthProperties;
import com.thundax.kuzhambu.system.application.auth.dao.OAuthAuthorizationDao;
import com.thundax.kuzhambu.system.application.auth.dao.OAuthClientDao;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalAccessTokenDao;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalAuthSessionDao;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalLoginEventDao;
import com.thundax.kuzhambu.system.application.auth.dao.PrincipalRefreshTokenDao;
import com.thundax.kuzhambu.system.application.auth.entity.OAuthAuthorization;
import com.thundax.kuzhambu.system.application.auth.entity.OAuthClient;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalRefreshToken;
import com.thundax.kuzhambu.system.application.auth.entity.enums.OAuthClientStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalLoginEventType;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.application.auth.entity.enums.PrincipalType;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.application.auth.entity.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.application.auth.exception.InvalidPasswordException;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalAuthService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityService;
import com.thundax.kuzhambu.system.application.auth.service.command.AuthenticateIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.service.command.AuthenticatePasswordCommand;
import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.thundax.kuzhambu.system.application.auth.service.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserIdCodec;
import com.thundax.kuzhambu.system.application.core.service.UserService;
import com.thundax.kuzhambu.common.core.crypto.Sha256Digest;
import com.thundax.kuzhambu.common.core.id.UuidHelper;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.AdminAuthService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.command.AdminAuthCommand;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider.GithubLoginProvider;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider.WecomLoginProvider;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.query.AdminAuthQuery;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthAccessTokenResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenQueryResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenRefreshResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.OAuth2AuthorizationViewResult;
import com.thundax.kuzhambu.system.interfaces.admin.configure.LoginProperties;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final int SESSION_RUNTIME_SAFETY_SECONDS = 10;
    private static final String ADMIN_CLIENT_ID = "admin-api";

    private final AuthProperties properties;
    private final LoginProperties loginProperties;
    private final PrincipalAuthSessionDao principalAuthSessionDao;
    private final PermissionService permissionService;
    private final PrincipalAuthService principalAuthService;
    private final PrincipalIdentityService principalIdentityService;
    private final UserService userService;

    @Autowired(required = false)
    private WecomLoginProvider wecomLoginProvider;

    @Autowired(required = false)
    private GithubLoginProvider githubLoginProvider;

    @Autowired(required = false)
    private OAuthAuthorizationDao oauthAuthorizationDao;

    @Autowired(required = false)
    private PrincipalAccessTokenDao principalAccessTokenDao;

    @Autowired(required = false)
    private OAuthClientDao oauthClientDao;

    @Autowired(required = false)
    private PrincipalRefreshTokenDao principalRefreshTokenDao;

    @Autowired(required = false)
    private PrincipalLoginEventDao principalLoginEventDao;

    public AdminAuthServiceImpl(
            AuthProperties properties,
            LoginProperties loginProperties,
            PrincipalAuthSessionDao principalAuthSessionDao,
            PermissionService permissionService,
            PrincipalAuthService principalAuthService,
            PrincipalIdentityService principalIdentityService,
            UserService userService) {
        this.properties = properties;
        this.loginProperties = loginProperties;
        this.principalAuthSessionDao = principalAuthSessionDao;
        this.permissionService = permissionService;
        this.principalAuthService = principalAuthService;
        this.principalIdentityService = principalIdentityService;
        this.userService = userService;
    }

    @Override
    public AuthAccessTokenResult createAccessToken(AdminAuthCommand command) {
        PrincipalAuthenticationMethod authenticationMethod = command.getAuthenticationMethod();
        if (authenticationMethod == null) {
            authenticationMethod = PrincipalAuthenticationMethod.PASSWORD;
        }
        PrincipalIdentityType identityType = command.getIdentityType();
        if (identityType == null) {
            identityType = PrincipalIdentityType.USER_ACCOUNT;
        }
        return createAccessToken(
                command.getUserId(),
                command.getLoginName(),
                command.getIp(),
                command.getUserAgent(),
                authenticationMethod,
                identityType);
    }

    @Override
    public AuthAccessTokenResult getAccessToken(AdminAuthQuery query) {
        return getAccessToken(query.getToken());
    }

    @Override
    public int deleteAccessTokensByUserId(AdminAuthCommand command) {
        return deleteAccessTokensByUserId(command.getUserId());
    }

    @Override
    public boolean validateToken(AdminAuthCommand command) {
        return validateToken(command.getAccessToken());
    }

    @Override
    public void activeAccessToken(AdminAuthCommand command) {
        activeAccessToken(command.getAccessToken());
    }

    @Override
    public void deleteAccessToken(AdminAuthCommand command) {
        deleteAccessToken(command.getAccessToken(), command.getIp(), command.getUserAgent());
    }

    @Override
    public AuthTokenQueryResult getTokenInfo(AdminAuthQuery query) {
        return queryToken(query.getToken());
    }

    @Override
    public AuthTokenRefreshResult refreshAccessToken(AdminAuthCommand command) {
        return refreshAccessToken(
                command.getClientId(), command.getRefreshToken(), command.getIp(), command.getUserAgent());
    }

    @Override
    public OAuth2AuthorizationViewResult authorizeOAuth2(AdminAuthCommand command) {
        return authorizeOAuth2(
                command.getClientId(), command.getRedirectUri(), command.getScopes(), command.getState());
    }

    @Override
    public OAuth2AuthorizationDecisionResult decideOAuth2(AdminAuthCommand command) {
        return decideOAuth2(
                command.getClientId(),
                command.getRedirectUri(),
                command.getScopes(),
                command.getState(),
                command.getCodeChallenge(),
                command.getCodeChallengeMethod(),
                command.getUserId(),
                command.isApproved(),
                command.getIp(),
                command.getUserAgent());
    }

    @Override
    public AuthTokenRefreshResult exchangeOAuth2Token(AdminAuthCommand command) {
        return exchangeOAuth2Token(
                command.getClientId(),
                command.getClientSecret(),
                command.getGrantType(),
                command.getRedirectUri(),
                command.getAuthorizationCode(),
                command.getCodeVerifier(),
                command.getRefreshToken(),
                command.getIp(),
                command.getUserAgent());
    }

    @Override
    public boolean revokeAuthorizationCode(AdminAuthCommand command) {
        return revokeAuthorizationCode(command.getAuthorizationCode());
    }

    @Override
    public boolean revokeOAuth2Token(AdminAuthCommand command) {
        return revokeOAuth2Token(command.getClientId(), command.getClientSecret(), command.getToken());
    }

    @Override
    public void invalidateSessionByToken(AdminAuthCommand command) {
        invalidateSessionByToken(command.getToken(), command.getReason());
    }

    @Override
    public int invalidateSessionsByUserId(AdminAuthCommand command) {
        return invalidateSessionsByUserId(command.getUserId(), command.getReason());
    }

    @Override
    public User authenticatePassword(AdminAuthCommand command) {
        return authenticatePassword(
                command.getLoginName(), command.getPlainPassword(), command.getIp(), command.getUserAgent());
    }

    @Override
    public User authenticateSms(AdminAuthCommand command) {
        return authenticateSms(command.getMobile(), command.getIp(), command.getUserAgent());
    }

    @Override
    public User authenticateWecom(AdminAuthCommand command) {
        return authenticateWecom(command.getCode(), command.getIp(), command.getUserAgent());
    }

    @Override
    public User authenticateGithub(AdminAuthCommand command) {
        return authenticateGithub(command.getCode(), command.getIp(), command.getUserAgent());
    }

    @Override
    public void recordLoginFailed(AdminAuthCommand command) {
        recordLoginFailed(
                command.getAuthenticationMethod(),
                command.getIdentityType(),
                command.getIp(),
                command.getUserAgent(),
                command.getReason());
    }

    @Override
    public void validatePassword(AdminAuthCommand command) {
        validatePassword(command.getUser(), command.getPlainPassword());
    }

    @NonNull
    private AuthAccessTokenResult createAccessToken(UserId userId) {
        return createAccessToken(userId, null);
    }

    @NonNull
    private AuthAccessTokenResult createAccessToken(UserId userId, String loginName) {
        return createAccessToken(userId, loginName, null, null);
    }

    @NonNull
    private AuthAccessTokenResult createAccessToken(UserId userId, String loginName, String ip, String userAgent) {
        return createAccessToken(
                userId,
                loginName,
                ip,
                userAgent,
                PrincipalAuthenticationMethod.PASSWORD,
                PrincipalIdentityType.USER_ACCOUNT);
    }

    @NonNull
    private AuthAccessTokenResult createAccessToken(
            UserId userId,
            String loginName,
            String ip,
            String userAgent,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType) {
        Date now = new Date();
        String token = UuidHelper.compact();
        PrincipalAccessToken accessToken = buildPrincipalAccessToken(
                token,
                ADMIN_CLIENT_ID,
                PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)),
                new LinkedHashSet<>(),
                now,
                properties.getLoginExpiredSeconds());
        PrincipalAuthSession session = PrincipalAuthSession.create(
                accessToken.getPrincipalKey(), ADMIN_CLIENT_ID, now, properties.getLoginExpiredSeconds());
        principalAuthSessionDao.insert(session, runtimeExpiredSeconds(properties.getLoginExpiredSeconds()));
        accessToken.setSessionId(session.getId());
        accessToken.setId(requirePrincipalAccessTokenDao().insert(accessToken, token));
        permissionService.createPermissions(token, UserIdCodec.toStringValue(userId));
        String refreshToken = createPrincipalRefreshToken(accessToken, ADMIN_CLIENT_ID, now);
        if (StringUtils.isNotBlank(loginName)) {
            writeLoginEvent(
                    accessToken.getPrincipalKey(),
                    ADMIN_CLIENT_ID,
                    PrincipalLoginEventType.LOGIN_SUCCESS,
                    authenticationMethod,
                    identityType,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_NONE);
        }
        return new AuthAccessTokenResult(token, refreshToken, accessToken);
    }

    private AuthAccessTokenResult getAccessToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        PrincipalAccessToken accessToken = requirePrincipalAccessTokenDao().getByToken(token);
        if (accessToken == null
                || !StringUtils.equals(ADMIN_CLIENT_ID, accessToken.getClientId())
                || !accessToken.canAccess(new Date())) {
            return null;
        }
        PrincipalAuthSession session = getActivePrincipalAuthSession(accessToken, new Date());
        if (session == null) {
            return null;
        }
        return new AuthAccessTokenResult(token, null, accessToken);
    }

    private int deleteAccessTokensByUserId(UserId userId) {
        int count = 0;
        List<PrincipalAccessToken> tokens = requirePrincipalAccessTokenDao()
                .listByPrincipalKeyAndClientIdAndStatus(
                        PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)),
                        ADMIN_CLIENT_ID,
                        PrincipalTokenStatus.ACTIVE);
        for (PrincipalAccessToken token : tokens) {
            if (token != null && token.isActive()) {
                token.revoke();
                requirePrincipalAccessTokenDao().updateStatus(token);
                principalAuthSessionDao.deleteById(token.getSessionId());
                count++;
            }
        }
        return count;
    }

    private boolean validateToken(AuthAccessTokenResult accessToken) {
        return accessToken != null
                && accessToken.getPrincipalAccessToken() != null
                && accessToken.getPrincipalAccessToken().canAccess(new Date());
    }

    private void activeAccessToken(AuthAccessTokenResult accessToken) {
        touchPrincipalAuthSession(accessToken.getPrincipalAccessToken());
    }

    private void deleteAccessToken(AuthAccessTokenResult accessToken) {
        deleteAccessToken(accessToken, null, null);
    }

    private void deleteAccessToken(AuthAccessTokenResult accessToken, String ip, String userAgent) {
        if (accessToken == null) {
            return;
        }
        PrincipalAccessToken principalAccessToken = accessToken.getPrincipalAccessToken();
        if (principalAccessToken != null && principalAccessToken.isActive()) {
            principalAccessToken.revoke();
            requirePrincipalAccessTokenDao().updateStatus(principalAccessToken);
        }
        deletePrincipalAuthSession(principalAccessToken);
        if (principalAccessToken != null) {
            writeLoginEvent(
                    principalAccessToken.getPrincipalKey(),
                    principalAccessToken.getClientId(),
                    PrincipalLoginEventType.LOGOUT,
                    PrincipalAuthenticationMethod.PASSWORD,
                    null,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_USER_LOGOUT);
        }
    }

    private AuthTokenQueryResult queryToken(String token) {
        AuthTokenQueryResult oauthResult = queryOAuthAccessToken(token);
        if (oauthResult != null) {
            return oauthResult;
        }
        AuthAccessTokenResult accessToken = getAccessToken(token);
        if (accessToken == null || !validateToken(accessToken)) {
            return AuthTokenQueryResult.inactive(token);
        }
        PrincipalAuthSession session = getActivePrincipalAuthSession(accessToken.getPrincipalAccessToken(), new Date());
        if (session == null) {
            return AuthTokenQueryResult.inactive(token);
        }
        User user = getUser(UserIdCodec.toDomain(session.getPrincipalKey().getPrincipalId()));
        if (user == null || !user.isEnable()) {
            return AuthTokenQueryResult.inactive(token);
        }
        return AuthTokenQueryResult.active(token, session, user, getAccountLoginName(user.getId()));
    }

    private AuthTokenQueryResult queryOAuthAccessToken(String token) {
        if (principalAccessTokenDao == null) {
            return null;
        }
        PrincipalAccessToken accessToken = principalAccessTokenDao.getByToken(token);
        if (accessToken == null) {
            return null;
        }
        if (StringUtils.equals(ADMIN_CLIENT_ID, accessToken.getClientId())) {
            return null;
        }
        if (!accessToken.canAccess(new Date())) {
            return AuthTokenQueryResult.inactive(token);
        }
        PrincipalAuthSession session = getActivePrincipalAuthSession(accessToken, new Date());
        if (session == null) {
            return AuthTokenQueryResult.inactive(token);
        }
        User user = getUser(UserIdCodec.toDomain(accessToken.getPrincipalKey().getPrincipalId()));
        if (user == null || !user.isEnable()) {
            return AuthTokenQueryResult.inactive(token);
        }
        return AuthTokenQueryResult.active(token, accessToken, session, user, getAccountLoginName(user.getId()));
    }

    private AuthTokenRefreshResult refreshAccessToken(String clientId, String refreshToken) {
        return refreshAccessToken(clientId, refreshToken, null, null);
    }

    private AuthTokenRefreshResult refreshAccessToken(
            String clientId, String refreshToken, String ip, String userAgent) {
        if (principalRefreshTokenDao == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        String requestedClientId = StringUtils.defaultIfBlank(clientId, ADMIN_CLIENT_ID);
        PrincipalRefreshToken current = principalRefreshTokenDao.getByToken(refreshToken);
        Date now = new Date();
        if (current == null
                || !current.canRefresh(now)
                || !StringUtils.equals(requestedClientId, current.getClientId())) {
            throw AdminResponseExceptions.invalidToken();
        }
        current.markUsed();
        principalRefreshTokenDao.updateStatus(current);

        AuthAccessTokenResult accessToken = createAccessToken(
                UserIdCodec.toDomain(current.getPrincipalKey().getPrincipalId()), null, ip, userAgent);
        writeLoginEvent(
                current.getPrincipalKey(),
                requestedClientId,
                PrincipalLoginEventType.TOKEN_REFRESH,
                PrincipalAuthenticationMethod.REFRESH_TOKEN,
                null,
                ip,
                userAgent,
                PrincipalLoginEvent.REASON_NONE);
        return new AuthTokenRefreshResult(accessToken, accessToken.getRefreshToken());
    }

    private OAuth2AuthorizationViewResult authorizeOAuth2(
            String clientId, String redirectUri, List<String> scopes, String state) {
        OAuthClient client = validateOAuthClient(clientId, redirectUri, scopes);
        OAuth2AuthorizationViewResult result = new OAuth2AuthorizationViewResult();
        result.setClientId(client.getClientId());
        result.setClientName(client.getClientName());
        result.setRedirectUri(redirectUri);
        result.setScopes(toScopeSet(scopes));
        result.setState(state);
        return result;
    }

    private OAuth2AuthorizationDecisionResult decideOAuth2(
            String clientId,
            String redirectUri,
            List<String> scopes,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            UserId userId,
            boolean approved) {
        return decideOAuth2(
                clientId, redirectUri, scopes, state, codeChallenge, codeChallengeMethod, userId, approved, null, null);
    }

    private OAuth2AuthorizationDecisionResult decideOAuth2(
            String clientId,
            String redirectUri,
            List<String> scopes,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            UserId userId,
            boolean approved,
            String ip,
            String userAgent) {
        validateOAuthClient(clientId, redirectUri, scopes);
        OAuth2AuthorizationDecisionResult result = new OAuth2AuthorizationDecisionResult();
        result.setApproved(approved);
        result.setState(state);
        if (!approved) {
            writeLoginEvent(
                    PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)),
                    clientId,
                    PrincipalLoginEventType.OAUTH_AUTHORIZED,
                    PrincipalAuthenticationMethod.OAUTH_CODE,
                    null,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_OAUTH_DENIED);
            return result;
        }
        if (oauthAuthorizationDao == null) {
            throw AdminResponseExceptions.oauth2AuthorizationNotConfigured();
        }
        Date now = new Date();
        OAuthAuthorization authorization = new OAuthAuthorization();
        authorization.setAuthorizationCode(UuidHelper.compact());
        authorization.setClientId(clientId);
        authorization.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)));
        authorization.setRedirectUri(redirectUri);
        authorization.setScopes(toScopeSet(scopes));
        authorization.setState(state);
        authorization.setCodeChallenge(codeChallenge);
        authorization.setCodeChallengeMethod(codeChallengeMethod);
        authorization.setIssuedAt(now);
        authorization.setExpireAt(new Date(now.getTime() + 300000L));
        authorization.setId(oauthAuthorizationDao.insert(authorization));
        result.setAuthorizationCode(authorization.getAuthorizationCode());
        writeLoginEvent(
                authorization.getPrincipalKey(),
                clientId,
                PrincipalLoginEventType.OAUTH_AUTHORIZED,
                PrincipalAuthenticationMethod.OAUTH_CODE,
                null,
                ip,
                userAgent,
                PrincipalLoginEvent.REASON_NONE);
        return result;
    }

    private AuthTokenRefreshResult exchangeOAuth2Token(
            String clientId,
            String clientSecret,
            String grantType,
            String redirectUri,
            String authorizationCode,
            String codeVerifier,
            String refreshToken) {
        return exchangeOAuth2Token(
                clientId,
                clientSecret,
                grantType,
                redirectUri,
                authorizationCode,
                codeVerifier,
                refreshToken,
                null,
                null);
    }

    private AuthTokenRefreshResult exchangeOAuth2Token(
            String clientId,
            String clientSecret,
            String grantType,
            String redirectUri,
            String authorizationCode,
            String codeVerifier,
            String refreshToken,
            String ip,
            String userAgent) {
        OAuthClient client = validateOAuthClientSecret(clientId, clientSecret);
        if (!client.supportsGrantType(grantType)) {
            throw AdminResponseExceptions.oauth2GrantTypeUnsupported();
        }
        if ("authorization_code".equals(grantType)) {
            return exchangeAuthorizationCode(client, redirectUri, authorizationCode, codeVerifier, ip, userAgent);
        }
        if ("refresh_token".equals(grantType)) {
            return refreshOAuth2Token(client, refreshToken, ip, userAgent);
        }
        throw AdminResponseExceptions.oauth2GrantTypeUnsupported();
    }

    private AuthTokenRefreshResult exchangeAuthorizationCode(
            OAuthClient client,
            String redirectUri,
            String authorizationCode,
            String codeVerifier,
            String ip,
            String userAgent) {
        if (oauthAuthorizationDao == null) {
            throw AdminResponseExceptions.oauth2AuthorizationNotConfigured();
        }
        OAuthAuthorization authorization = oauthAuthorizationDao.getByAuthorizationCode(authorizationCode);
        Date now = new Date();
        if (authorization == null
                || !authorization.canConsume(now)
                || !StringUtils.equals(client.getClientId(), authorization.getClientId())
                || !StringUtils.equals(redirectUri, authorization.getRedirectUri())
                || !verifyPkce(authorization, codeVerifier)) {
            throw AdminResponseExceptions.invalidToken();
        }
        authorization.markUsed(now);
        oauthAuthorizationDao.updateUsed(authorization);
        long accessTokenTtlSeconds = accessTokenTtlSeconds(client);
        PrincipalAuthSession session = PrincipalAuthSession.create(
                authorization.getPrincipalKey(), client.getClientId(), now, accessTokenTtlSeconds);
        principalAuthSessionDao.insert(session, runtimeExpiredSeconds(accessTokenTtlSeconds));
        AuthAccessTokenResult oauthAccessToken = createOAuthAccessToken(client, authorization, session, now);
        String refreshToken = principalRefreshTokenDao == null
                ? null
                : createPrincipalRefreshToken(oauthAccessToken.getPrincipalAccessToken(), client.getClientId(), now);
        return new AuthTokenRefreshResult(oauthAccessToken, refreshToken, oauthAccessToken.getToken());
    }

    private AuthTokenRefreshResult refreshOAuth2Token(
            OAuthClient client, String refreshToken, String ip, String userAgent) {
        if (principalRefreshTokenDao == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        PrincipalRefreshToken current = principalRefreshTokenDao.getByToken(refreshToken);
        Date now = new Date();
        if (current == null
                || !current.canRefresh(now)
                || !StringUtils.equals(client.getClientId(), current.getClientId())) {
            throw AdminResponseExceptions.invalidToken();
        }
        current.markUsed();
        principalRefreshTokenDao.updateStatus(current);

        PrincipalAuthSession session = getActivePrincipalAuthSession(current, now);
        if (session == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        AuthAccessTokenResult oauthAccessToken = createOAuthAccessToken(client, current, session, now);
        String nextRefreshToken =
                createPrincipalRefreshToken(oauthAccessToken.getPrincipalAccessToken(), client.getClientId(), now);
        writeLoginEvent(
                current.getPrincipalKey(),
                client.getClientId(),
                PrincipalLoginEventType.TOKEN_REFRESH,
                PrincipalAuthenticationMethod.REFRESH_TOKEN,
                null,
                ip,
                userAgent,
                PrincipalLoginEvent.REASON_NONE);
        return new AuthTokenRefreshResult(oauthAccessToken, nextRefreshToken, oauthAccessToken.getToken());
    }

    private boolean revokeAuthorizationCode(String authorizationCode) {
        if (oauthAuthorizationDao == null) {
            throw AdminResponseExceptions.oauth2AuthorizationNotConfigured();
        }
        return oauthAuthorizationDao.deleteByAuthorizationCode(authorizationCode) > 0;
    }

    private boolean revokeOAuth2Token(String clientId, String clientSecret, String token) {
        validateOAuthClientSecret(clientId, clientSecret);
        Date now = new Date();
        boolean revoked = false;
        if (principalAccessTokenDao != null) {
            PrincipalAccessToken accessToken = principalAccessTokenDao.getByToken(token);
            if (accessToken != null && accessToken.isActive()) {
                accessToken.revoke();
                principalAccessTokenDao.updateStatus(accessToken);
                principalAuthSessionDao.deleteById(accessToken.getSessionId());
                revoked = true;
            }
        }
        if (principalRefreshTokenDao != null) {
            PrincipalRefreshToken refreshToken = principalRefreshTokenDao.getByToken(token);
            if (refreshToken != null && refreshToken.isActive()) {
                refreshToken.revoke();
                principalRefreshTokenDao.updateStatus(refreshToken);
                principalAuthSessionDao.deleteById(refreshToken.getSessionId());
                revoked = true;
            }
        }
        return revoked;
    }

    private void invalidateSessionByToken(String token, String reason) {
        invalidatePrincipalAuthSession(token);
    }

    private int invalidateSessionsByUserId(UserId userId, String reason) {
        List<PrincipalAccessToken> tokens = requirePrincipalAccessTokenDao()
                .listByPrincipalKeyAndClientIdAndStatus(
                        PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)),
                        ADMIN_CLIENT_ID,
                        PrincipalTokenStatus.ACTIVE);
        int count = 0;
        for (PrincipalAccessToken token : tokens) {
            if (token != null && token.isActive()) {
                token.revoke();
                requirePrincipalAccessTokenDao().updateStatus(token);
                principalAuthSessionDao.deleteById(token.getSessionId());
                count++;
            }
        }
        return count;
    }

    private User authenticatePassword(String loginName, String plainPassword) {
        return authenticatePassword(loginName, plainPassword, null, null);
    }

    private User authenticatePassword(String loginName, String plainPassword, String ip, String userAgent) {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticatePassword(new AuthenticatePasswordCommand(
                    PrincipalIdentityType.USER_ACCOUNT,
                    loginName,
                    PrincipalCredentialType.USER_PASSWORD,
                    plainPassword,
                    passwordPolicy()));
        } catch (InvalidPasswordException e) {
            recordLoginFailed(
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_INVALID_CREDENTIAL);
            throw AdminResponseExceptions.invalidUsernamePassword();
        }

        User user = getUser(UserIdCodec.toDomain(identity.getPrincipalKey().getPrincipalId()));
        if (user == null) {
            recordLoginFailed(
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_PRINCIPAL_NOT_FOUND);
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        if (!user.isEnable()) {
            writeLoginEvent(
                    identity.getPrincipalKey(),
                    ADMIN_CLIENT_ID,
                    PrincipalLoginEventType.LOGIN_FAILED,
                    PrincipalAuthenticationMethod.PASSWORD,
                    PrincipalIdentityType.USER_ACCOUNT,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_ACCOUNT_DISABLED);
            throw AdminResponseExceptions.bannedAccount();
        }
        return user;
    }

    private User authenticateSms(String mobile) {
        return authenticateSms(mobile, null, null);
    }

    private User authenticateSms(String mobile, String ip, String userAgent) {
        return authenticateIdentity(
                PrincipalIdentityType.USER_MOBILE, mobile, PrincipalAuthenticationMethod.SMS_CODE, ip, userAgent);
    }

    private User authenticateWecom(String code) {
        return authenticateWecom(code, null, null);
    }

    private User authenticateWecom(String code, String ip, String userAgent) {
        if (wecomLoginProvider == null) {
            throw AdminResponseExceptions.wecomLoginNotConfigured();
        }
        return authenticateIdentity(
                PrincipalIdentityType.USER_WECOM,
                wecomLoginProvider.resolveIdentity(code),
                PrincipalAuthenticationMethod.WECOM,
                ip,
                userAgent);
    }

    private User authenticateGithub(String code) {
        return authenticateGithub(code, null, null);
    }

    private User authenticateGithub(String code, String ip, String userAgent) {
        if (githubLoginProvider == null) {
            throw AdminResponseExceptions.githubLoginNotConfigured();
        }
        return authenticateIdentity(
                PrincipalIdentityType.USER_GITHUB,
                githubLoginProvider.resolveIdentity(code),
                PrincipalAuthenticationMethod.GITHUB,
                ip,
                userAgent);
    }

    private void validatePassword(User user, String plainPassword) {
        if (user == null) {
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        String loginName = getAccountLoginName(user.getId());
        if (StringUtils.isBlank(loginName)) {
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        authenticatePassword(loginName, plainPassword);
    }

    private void recordLoginFailed(
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        writeLoginEvent(
                null,
                ADMIN_CLIENT_ID,
                PrincipalLoginEventType.LOGIN_FAILED,
                authenticationMethod,
                identityType,
                ip,
                userAgent,
                reason);
    }

    private User authenticateIdentity(
            PrincipalIdentityType identityType,
            String identityValue,
            PrincipalAuthenticationMethod authenticationMethod,
            String ip,
            String userAgent) {
        PrincipalIdentity identity;
        try {
            identity = principalAuthService.authenticateIdentity(
                    new AuthenticateIdentityCommand(identityType, identityValue));
        } catch (InvalidPasswordException e) {
            recordLoginFailed(
                    authenticationMethod, identityType, ip, userAgent, PrincipalLoginEvent.REASON_IDENTITY_NOT_FOUND);
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        User user = getUser(UserIdCodec.toDomain(identity.getPrincipalKey().getPrincipalId()));
        if (user == null) {
            recordLoginFailed(
                    authenticationMethod, identityType, ip, userAgent, PrincipalLoginEvent.REASON_PRINCIPAL_NOT_FOUND);
            throw AdminResponseExceptions.invalidUsernamePassword();
        }
        if (!user.isEnable()) {
            writeLoginEvent(
                    identity.getPrincipalKey(),
                    ADMIN_CLIENT_ID,
                    PrincipalLoginEventType.LOGIN_FAILED,
                    authenticationMethod,
                    identityType,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_ACCOUNT_DISABLED);
            throw AdminResponseExceptions.bannedAccount();
        }
        return user;
    }

    private PrincipalAuthSession getActivePrincipalAuthSession(PrincipalAccessToken accessToken, Date now) {
        if (accessToken == null || accessToken.getSessionId() == null) {
            return null;
        }
        PrincipalAuthSession session = principalAuthSessionDao.getById(accessToken.getSessionId());
        if (session == null || session.isExpired(now)) {
            return null;
        }
        return session;
    }

    private User getUser(UserId userId) {
        return userService.get(userId);
    }

    private PrincipalAuthSession getActivePrincipalAuthSession(PrincipalRefreshToken refreshToken, Date now) {
        if (refreshToken == null || refreshToken.getSessionId() == null) {
            return null;
        }
        PrincipalAuthSession session = principalAuthSessionDao.getById(refreshToken.getSessionId());
        if (session == null || session.isExpired(now)) {
            return null;
        }
        return session;
    }

    private void touchPrincipalAuthSession(PrincipalAccessToken accessToken) {
        PrincipalAuthSession session = getActivePrincipalAuthSession(accessToken, new Date());
        if (session == null) {
            return;
        }
        Date now = new Date();
        principalAuthSessionDao.touch(session.getId(), now, runtimeExpiredSeconds());
    }

    private void deletePrincipalAuthSession(PrincipalAccessToken accessToken) {
        if (accessToken != null) {
            principalAuthSessionDao.deleteById(accessToken.getSessionId());
        }
    }

    private void invalidatePrincipalAuthSession(String token) {
        AuthAccessTokenResult accessToken = getAccessToken(token);
        if (accessToken != null && accessToken.getPrincipalAccessToken() != null) {
            PrincipalAccessToken principalAccessToken = accessToken.getPrincipalAccessToken();
            principalAccessToken.revoke();
            requirePrincipalAccessTokenDao().updateStatus(principalAccessToken);
            principalAuthSessionDao.deleteById(principalAccessToken.getSessionId());
        }
    }

    private void writeLoginEvent(
            PrincipalKey principalKey,
            String clientId,
            PrincipalLoginEventType eventType,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        if (principalLoginEventDao == null) {
            return;
        }
        PrincipalLoginEvent event = new PrincipalLoginEvent();
        event.setPrincipalKey(principalKey);
        event.setClientId(clientId);
        event.setEventType(eventType);
        event.setAuthenticationMethod(authenticationMethod);
        event.setIdentityType(identityType);
        event.setOccurredAt(new Date());
        event.setIp(ip);
        event.setUserAgent(userAgent);
        event.setReason(reason);
        principalLoginEventDao.insert(event);
    }

    private int runtimeExpiredSeconds() {
        return runtimeExpiredSeconds(properties.getLoginExpiredSeconds());
    }

    private int runtimeExpiredSeconds(long ttlSeconds) {
        return (int) ttlSeconds + SESSION_RUNTIME_SAFETY_SECONDS;
    }

    private String createPrincipalRefreshToken(PrincipalAccessToken accessToken, String clientId, Date issuedAt) {
        String refreshToken = UuidHelper.compact();
        PrincipalRefreshToken entity = new PrincipalRefreshToken();
        entity.setTokenCode(PrincipalRefreshTokenCode.of(UuidHelper.compact()));
        entity.setAccessTokenId(accessToken.getId());
        entity.setClientId(clientId);
        entity.setSessionId(accessToken.getSessionId());
        entity.setPrincipalKey(accessToken.getPrincipalKey());
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(new Date(issuedAt.getTime() + refreshTokenTtlSeconds(clientId) * 1000L));
        entity.setStatus(PrincipalTokenStatus.ACTIVE);
        entity.setId(requirePrincipalRefreshTokenDao().insert(entity, refreshToken));
        return refreshToken;
    }

    private AuthAccessTokenResult createOAuthAccessToken(
            OAuthClient client, OAuthAuthorization authorization, PrincipalAuthSession session, Date issuedAt) {
        String token = UuidHelper.compact();
        PrincipalAccessToken entity = buildPrincipalAccessToken(
                token,
                client.getClientId(),
                authorization.getPrincipalKey(),
                authorization.getScopes(),
                issuedAt,
                accessTokenTtlSeconds(client));
        entity.setSessionId(session.getId());
        entity.setId(requirePrincipalAccessTokenDao().insert(entity, token));
        return new AuthAccessTokenResult(token, null, entity);
    }

    private AuthAccessTokenResult createOAuthAccessToken(
            OAuthClient client, PrincipalRefreshToken refreshToken, PrincipalAuthSession session, Date issuedAt) {
        String token = UuidHelper.compact();
        PrincipalAccessToken entity = buildPrincipalAccessToken(
                token,
                client.getClientId(),
                refreshToken.getPrincipalKey(),
                new LinkedHashSet<>(),
                issuedAt,
                accessTokenTtlSeconds(client));
        entity.setSessionId(session.getId());
        entity.setId(requirePrincipalAccessTokenDao().insert(entity, token));
        return new AuthAccessTokenResult(token, null, entity);
    }

    private PrincipalAccessToken buildPrincipalAccessToken(
            String token,
            String clientId,
            PrincipalKey principalKey,
            Set<String> scopes,
            Date issuedAt,
            long ttlSeconds) {
        PrincipalAccessToken entity = new PrincipalAccessToken();
        entity.setTokenCode(PrincipalAccessTokenCode.of(UuidHelper.compact()));
        entity.setClientId(clientId);
        entity.setPrincipalKey(principalKey);
        entity.setScopes(scopes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(scopes));
        entity.setIssuedAt(issuedAt);
        entity.setExpireAt(new Date(issuedAt.getTime() + ttlSeconds * 1000L));
        entity.setStatus(PrincipalTokenStatus.ACTIVE);
        return entity;
    }

    private PrincipalAccessTokenDao requirePrincipalAccessTokenDao() {
        if (principalAccessTokenDao == null) {
            throw new IllegalStateException("principal access token dao 未配置");
        }
        return principalAccessTokenDao;
    }

    private PrincipalRefreshTokenDao requirePrincipalRefreshTokenDao() {
        if (principalRefreshTokenDao == null) {
            throw new IllegalStateException("principal refresh token dao 未配置");
        }
        return principalRefreshTokenDao;
    }

    private long refreshTokenTtlSeconds(String clientId) {
        if (oauthClientDao == null) {
            return 2592000L;
        }
        OAuthClient client = oauthClientDao.getByClientIdAndStatus(clientId, OAuthClientStatus.ENABLED);
        if (client == null || client.getRefreshTokenTtlSeconds() <= 0L) {
            return 2592000L;
        }
        return client.getRefreshTokenTtlSeconds();
    }

    private long accessTokenTtlSeconds(OAuthClient client) {
        if (client == null || client.getAccessTokenTtlSeconds() <= 0L) {
            return properties.getLoginExpiredSeconds();
        }
        return client.getAccessTokenTtlSeconds();
    }

    private OAuthClient validateOAuthClientSecret(String clientId, String clientSecret) {
        if (oauthClientDao == null) {
            throw AdminResponseExceptions.oauth2ClientNotConfigured();
        }
        OAuthClient client = oauthClientDao.getByClientIdAndStatus(clientId, OAuthClientStatus.ENABLED);
        if (client == null
                || !StringUtils.equals(Sha256Digest.hashBase64Url(clientSecret), client.getClientSecretHash())) {
            throw AdminResponseExceptions.oauth2ClientSecretInvalid();
        }
        return client;
    }

    private OAuthClient validateOAuthClient(String clientId, String redirectUri, List<String> scopes) {
        if (oauthClientDao == null) {
            throw AdminResponseExceptions.oauth2ClientNotConfigured();
        }
        OAuthClient client = oauthClientDao.getByClientIdAndStatus(clientId, OAuthClientStatus.ENABLED);
        Set<String> requestedScopes = toScopeSet(scopes);
        if (client == null || !client.supportsRedirectUri(redirectUri) || !client.supportsScopes(requestedScopes)) {
            throw AdminResponseExceptions.oauth2ClientRequestInvalid();
        }
        return client;
    }

    private Set<String> toScopeSet(List<String> scopes) {
        return scopes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(scopes);
    }

    private boolean verifyPkce(OAuthAuthorization authorization, String codeVerifier) {
        if (StringUtils.isBlank(authorization.getCodeChallenge())) {
            return true;
        }
        if (StringUtils.isBlank(codeVerifier)) {
            return false;
        }
        if ("S256".equalsIgnoreCase(authorization.getCodeChallengeMethod())) {
            return StringUtils.equals(authorization.getCodeChallenge(), Sha256Digest.hashBase64Url(codeVerifier));
        }
        return StringUtils.equals(authorization.getCodeChallenge(), codeVerifier);
    }

    private String getAccountLoginName(UserId userId) {
        if (userId == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.get(
                identityQuery(PrincipalKey.of(PrincipalType.USER, userId.value()), PrincipalIdentityType.USER_ACCOUNT));
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private PrincipalPasswordPolicyDTO passwordPolicy() {
        return new PrincipalPasswordPolicyDTO(
                loginProperties.getEnable(), loginProperties.getMaxFailCount(), loginProperties.getLockTime());
    }
}
