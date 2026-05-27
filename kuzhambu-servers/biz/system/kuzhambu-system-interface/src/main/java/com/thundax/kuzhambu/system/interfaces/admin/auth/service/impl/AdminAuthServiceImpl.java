package com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl;

import com.thundax.kuzhambu.common.core.id.UuidHelper;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.system.application.auth.command.AuthenticateIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.command.AuthenticatePasswordCommand;
import com.thundax.kuzhambu.system.application.auth.configure.AuthProperties;
import com.thundax.kuzhambu.system.application.auth.exception.InvalidPasswordException;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalAuthApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalRefreshToken;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalLoginEventType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAccessTokenRepository;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAuthSessionRepository;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalLoginEventRepository;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalRefreshTokenRepository;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.AdminAuthService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.PermissionService;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.command.AdminAuthCommand;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider.GithubLoginProvider;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.provider.WecomLoginProvider;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.query.AdminAuthQuery;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthAccessTokenResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenQueryResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenRefreshResult;
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
    private final PrincipalAuthSessionRepository principalAuthSessionRepository;
    private final PermissionService permissionService;
    private final PrincipalAuthApplicationService principalAuthService;
    private final PrincipalIdentityApplicationService principalIdentityService;
    private final UserApplicationService userService;

    @Autowired(required = false)
    private WecomLoginProvider wecomLoginProvider;

    @Autowired(required = false)
    private GithubLoginProvider githubLoginProvider;

    @Autowired(required = false)
    private PrincipalAccessTokenRepository principalAccessTokenRepository;

    @Autowired(required = false)
    private PrincipalRefreshTokenRepository principalRefreshTokenRepository;

    @Autowired(required = false)
    private PrincipalLoginEventRepository principalLoginEventRepository;

    public AdminAuthServiceImpl(
            AuthProperties properties,
            LoginProperties loginProperties,
            PrincipalAuthSessionRepository principalAuthSessionRepository,
            PermissionService permissionService,
            PrincipalAuthApplicationService principalAuthService,
            PrincipalIdentityApplicationService principalIdentityService,
            UserApplicationService userService) {
        this.properties = properties;
        this.loginProperties = loginProperties;
        this.principalAuthSessionRepository = principalAuthSessionRepository;
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
        principalAuthSessionRepository.insert(session, runtimeExpiredSeconds(properties.getLoginExpiredSeconds()));
        accessToken.setSessionId(session.getId());
        accessToken.setId(requirePrincipalAccessTokenRepository().insert(accessToken, token));
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
        PrincipalAccessToken accessToken =
                requirePrincipalAccessTokenRepository().getByToken(token);
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
        List<PrincipalAccessToken> tokens = requirePrincipalAccessTokenRepository()
                .listByPrincipalKeyAndClientIdAndStatus(
                        PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)),
                        ADMIN_CLIENT_ID,
                        PrincipalTokenStatus.ACTIVE);
        for (PrincipalAccessToken token : tokens) {
            if (token != null && token.isActive()) {
                token.revoke();
                requirePrincipalAccessTokenRepository().updateStatus(token);
                principalAuthSessionRepository.deleteById(token.getSessionId());
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
            requirePrincipalAccessTokenRepository().updateStatus(principalAccessToken);
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

    private AuthTokenRefreshResult refreshAccessToken(String clientId, String refreshToken) {
        return refreshAccessToken(clientId, refreshToken, null, null);
    }

    private AuthTokenRefreshResult refreshAccessToken(
            String clientId, String refreshToken, String ip, String userAgent) {
        if (principalRefreshTokenRepository == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        String requestedClientId = StringUtils.defaultIfBlank(clientId, ADMIN_CLIENT_ID);
        PrincipalRefreshToken current = principalRefreshTokenRepository.getByToken(refreshToken);
        Date now = new Date();
        if (current == null
                || !current.canRefresh(now)
                || !StringUtils.equals(requestedClientId, current.getClientId())) {
            throw AdminResponseExceptions.invalidToken();
        }
        current.markUsed();
        principalRefreshTokenRepository.updateStatus(current);

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

    private void invalidateSessionByToken(String token, String reason) {
        invalidatePrincipalAuthSession(token);
    }

    private int invalidateSessionsByUserId(UserId userId, String reason) {
        List<PrincipalAccessToken> tokens = requirePrincipalAccessTokenRepository()
                .listByPrincipalKeyAndClientIdAndStatus(
                        PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)),
                        ADMIN_CLIENT_ID,
                        PrincipalTokenStatus.ACTIVE);
        int count = 0;
        for (PrincipalAccessToken token : tokens) {
            if (token != null && token.isActive()) {
                token.revoke();
                requirePrincipalAccessTokenRepository().updateStatus(token);
                principalAuthSessionRepository.deleteById(token.getSessionId());
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
        PrincipalAuthSession session = principalAuthSessionRepository.getById(accessToken.getSessionId());
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
        PrincipalAuthSession session = principalAuthSessionRepository.getById(refreshToken.getSessionId());
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
        principalAuthSessionRepository.touch(session.getId(), now, runtimeExpiredSeconds());
    }

    private void deletePrincipalAuthSession(PrincipalAccessToken accessToken) {
        if (accessToken != null) {
            principalAuthSessionRepository.deleteById(accessToken.getSessionId());
        }
    }

    private void invalidatePrincipalAuthSession(String token) {
        AuthAccessTokenResult accessToken = getAccessToken(token);
        if (accessToken != null && accessToken.getPrincipalAccessToken() != null) {
            PrincipalAccessToken principalAccessToken = accessToken.getPrincipalAccessToken();
            principalAccessToken.revoke();
            requirePrincipalAccessTokenRepository().updateStatus(principalAccessToken);
            principalAuthSessionRepository.deleteById(principalAccessToken.getSessionId());
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
        if (principalLoginEventRepository == null) {
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
        principalLoginEventRepository.insert(event);
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
        entity.setId(requirePrincipalRefreshTokenRepository().insert(entity, refreshToken));
        return refreshToken;
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

    private PrincipalAccessTokenRepository requirePrincipalAccessTokenRepository() {
        if (principalAccessTokenRepository == null) {
            throw new IllegalStateException("principal access token dao 未配置");
        }
        return principalAccessTokenRepository;
    }

    private PrincipalRefreshTokenRepository requirePrincipalRefreshTokenRepository() {
        if (principalRefreshTokenRepository == null) {
            throw new IllegalStateException("principal refresh token dao 未配置");
        }
        return principalRefreshTokenRepository;
    }

    private long refreshTokenTtlSeconds(String clientId) {
        return 2592000L;
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
