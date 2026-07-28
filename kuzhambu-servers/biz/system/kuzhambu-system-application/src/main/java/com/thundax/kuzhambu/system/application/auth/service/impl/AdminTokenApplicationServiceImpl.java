package com.thundax.kuzhambu.system.application.auth.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.id.UuidHelper;
import com.thundax.kuzhambu.system.application.auth.configure.AuthProperties;
import com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery;
import com.thundax.kuzhambu.system.application.auth.result.AdminAccessTokenResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenQueryResult;
import com.thundax.kuzhambu.system.application.auth.result.AdminTokenRefreshResult;
import com.thundax.kuzhambu.system.application.auth.service.AdminTokenApplicationService;
import com.thundax.kuzhambu.system.application.auth.service.PrincipalIdentityApplicationService;
import com.thundax.kuzhambu.system.application.core.service.UserApplicationService;
import com.thundax.kuzhambu.system.domain.auth.codec.PrincipalClientIdCodec;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalIdentity;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalLoginEvent;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalRefreshToken;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalLoginEventType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalTokenStatus;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalClientId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAccessTokenRepository;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalAuthSessionRepository;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalLoginEventRepository;
import com.thundax.kuzhambu.system.domain.auth.repository.PrincipalRefreshTokenRepository;
import com.thundax.kuzhambu.system.domain.core.codec.UserIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class AdminTokenApplicationServiceImpl implements AdminTokenApplicationService {

    private static final int SESSION_RUNTIME_SAFETY_SECONDS = 10;
    private static final PrincipalClientId ADMIN_CLIENT_ID = PrincipalClientIdCodec.toDomain("admin-api");

    private final AuthProperties properties;
    private final PrincipalAuthSessionRepository principalAuthSessionRepository;
    private final PrincipalIdentityApplicationService principalIdentityService;
    private final UserApplicationService userService;
    private final ObjectProvider<PrincipalAccessTokenRepository> principalAccessTokenRepositoryProvider;
    private final ObjectProvider<PrincipalRefreshTokenRepository> principalRefreshTokenRepositoryProvider;
    private final ObjectProvider<PrincipalLoginEventRepository> principalLoginEventRepositoryProvider;

    public AdminTokenApplicationServiceImpl(
            AuthProperties properties,
            PrincipalAuthSessionRepository principalAuthSessionRepository,
            PrincipalIdentityApplicationService principalIdentityService,
            UserApplicationService userService,
            ObjectProvider<PrincipalAccessTokenRepository> principalAccessTokenRepositoryProvider,
            ObjectProvider<PrincipalRefreshTokenRepository> principalRefreshTokenRepositoryProvider,
            ObjectProvider<PrincipalLoginEventRepository> principalLoginEventRepositoryProvider) {
        this.properties = properties;
        this.principalAuthSessionRepository = principalAuthSessionRepository;
        this.principalIdentityService = principalIdentityService;
        this.userService = userService;
        this.principalAccessTokenRepositoryProvider = principalAccessTokenRepositoryProvider;
        this.principalRefreshTokenRepositoryProvider = principalRefreshTokenRepositoryProvider;
        this.principalLoginEventRepositoryProvider = principalLoginEventRepositoryProvider;
    }

    @Override
    @NonNull
    public AdminAccessTokenResult createAccessToken(
            UserId userId,
            String loginName,
            String ip,
            String userAgent,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType) {
        PrincipalAuthenticationMethod method =
                authenticationMethod == null ? PrincipalAuthenticationMethod.PASSWORD : authenticationMethod;
        PrincipalIdentityType type = identityType == null ? PrincipalIdentityType.USER_ACCOUNT : identityType;
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
        String refreshToken = createPrincipalRefreshToken(accessToken, ADMIN_CLIENT_ID, now);
        if (StringUtils.isNotBlank(loginName)) {
            writeLoginEvent(
                    accessToken.getPrincipalKey(),
                    ADMIN_CLIENT_ID,
                    PrincipalLoginEventType.LOGIN_SUCCESS,
                    method,
                    type,
                    ip,
                    userAgent,
                    PrincipalLoginEvent.REASON_NONE);
        }
        return new AdminAccessTokenResult(token, refreshToken, accessToken);
    }

    @Override
    public AdminAccessTokenResult getAccessToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        PrincipalAccessToken accessToken =
                requirePrincipalAccessTokenRepository().getByToken(token);
        if (accessToken == null
                || !ADMIN_CLIENT_ID.equals(accessToken.getClientId())
                || !accessToken.canAccess(new Date())) {
            return null;
        }
        PrincipalAuthSession session = getActivePrincipalAuthSession(accessToken, new Date());
        if (session == null) {
            return null;
        }
        return new AdminAccessTokenResult(token, null, accessToken);
    }

    @Override
    public int deleteAccessTokensByUserId(UserId userId) {
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

    @Override
    public boolean validateToken(String token) {
        AdminAccessTokenResult accessToken = getAccessToken(token);
        return accessToken != null
                && accessToken.getPrincipalAccessToken() != null
                && accessToken.getPrincipalAccessToken().canAccess(new Date());
    }

    @Override
    public void activeAccessToken(String token) {
        AdminAccessTokenResult accessToken = getAccessToken(token);
        if (accessToken == null) {
            return;
        }
        touchPrincipalAuthSession(accessToken.getPrincipalAccessToken());
    }

    @Override
    public void deleteAccessToken(String token, String ip, String userAgent) {
        AdminAccessTokenResult accessToken = getAccessToken(token);
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

    @Override
    public AdminTokenQueryResult getTokenInfo(String token) {
        AdminAccessTokenResult accessToken = getAccessToken(token);
        if (accessToken == null || !validateToken(token)) {
            return AdminTokenQueryResult.inactive(token);
        }
        PrincipalAuthSession session = getActivePrincipalAuthSession(accessToken.getPrincipalAccessToken(), new Date());
        if (session == null) {
            return AdminTokenQueryResult.inactive(token);
        }
        User user = getUser(UserIdCodec.toDomain(session.getPrincipalKey().getPrincipalId()));
        if (user == null || !user.isEnable()) {
            return AdminTokenQueryResult.inactive(token);
        }
        return AdminTokenQueryResult.active(token, session, user, getAccountLoginName(user.getId()));
    }

    @Override
    public AdminTokenRefreshResult refreshAccessToken(
            String clientId, String refreshToken, String ip, String userAgent) {
        PrincipalRefreshTokenRepository refreshTokenRepository =
                principalRefreshTokenRepositoryProvider.getIfAvailable();
        if (refreshTokenRepository == null) {
            throw invalidToken();
        }
        PrincipalClientId requestedClientId =
                PrincipalClientIdCodec.toDomain(StringUtils.defaultIfBlank(clientId, ADMIN_CLIENT_ID.value()));
        PrincipalRefreshToken current = refreshTokenRepository.getByToken(refreshToken);
        Date now = new Date();
        if (current == null || !current.canRefresh(now) || !requestedClientId.equals(current.getClientId())) {
            throw invalidToken();
        }
        if (refreshTokenRepository.markUsedIfActive(current, now) != 1) {
            throw invalidToken();
        }

        AdminAccessTokenResult accessToken = createAccessToken(
                UserIdCodec.toDomain(current.getPrincipalKey().getPrincipalId()), null, ip, userAgent, null, null);
        writeLoginEvent(
                current.getPrincipalKey(),
                requestedClientId,
                PrincipalLoginEventType.TOKEN_REFRESH,
                PrincipalAuthenticationMethod.REFRESH_TOKEN,
                null,
                ip,
                userAgent,
                PrincipalLoginEvent.REASON_NONE);
        return new AdminTokenRefreshResult(accessToken, accessToken.getRefreshToken());
    }

    @Override
    public void invalidateSessionByToken(String token, String reason) {
        invalidatePrincipalAuthSession(token);
    }

    @Override
    public int invalidateSessionsByUserId(UserId userId, String reason) {
        return deleteAccessTokensByUserId(userId);
    }

    @Override
    public void recordLoginFailed(
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        recordLoginFailed(null, authenticationMethod, identityType, ip, userAgent, reason);
    }

    @Override
    public void recordLoginFailed(
            PrincipalKey principalKey,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        writeLoginEvent(
                principalKey,
                ADMIN_CLIENT_ID,
                PrincipalLoginEventType.LOGIN_FAILED,
                authenticationMethod,
                identityType,
                ip,
                userAgent,
                reason);
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

    private void touchPrincipalAuthSession(PrincipalAccessToken accessToken) {
        PrincipalAuthSession session = getActivePrincipalAuthSession(accessToken, new Date());
        if (session == null) {
            return;
        }
        principalAuthSessionRepository.touch(session.getId(), new Date(), runtimeExpiredSeconds());
    }

    private void deletePrincipalAuthSession(PrincipalAccessToken accessToken) {
        if (accessToken != null) {
            principalAuthSessionRepository.deleteById(accessToken.getSessionId());
        }
    }

    private void invalidatePrincipalAuthSession(String token) {
        AdminAccessTokenResult accessToken = getAccessToken(token);
        if (accessToken != null && accessToken.getPrincipalAccessToken() != null) {
            PrincipalAccessToken principalAccessToken = accessToken.getPrincipalAccessToken();
            principalAccessToken.revoke();
            requirePrincipalAccessTokenRepository().updateStatus(principalAccessToken);
            principalAuthSessionRepository.deleteById(principalAccessToken.getSessionId());
        }
    }

    private void writeLoginEvent(
            PrincipalKey principalKey,
            PrincipalClientId clientId,
            PrincipalLoginEventType eventType,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        PrincipalLoginEventRepository loginEventRepository = principalLoginEventRepositoryProvider.getIfAvailable();
        if (loginEventRepository == null) {
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
        loginEventRepository.insert(event);
    }

    private int runtimeExpiredSeconds() {
        return runtimeExpiredSeconds(properties.getLoginExpiredSeconds());
    }

    private int runtimeExpiredSeconds(long ttlSeconds) {
        return (int) ttlSeconds + SESSION_RUNTIME_SAFETY_SECONDS;
    }

    private String createPrincipalRefreshToken(
            PrincipalAccessToken accessToken, PrincipalClientId clientId, Date issuedAt) {
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
            PrincipalClientId clientId,
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
        PrincipalAccessTokenRepository repository = principalAccessTokenRepositoryProvider.getIfAvailable();
        if (repository == null) {
            throw new IllegalStateException("principal access token dao 未配置");
        }
        return repository;
    }

    private PrincipalRefreshTokenRepository requirePrincipalRefreshTokenRepository() {
        PrincipalRefreshTokenRepository repository = principalRefreshTokenRepositoryProvider.getIfAvailable();
        if (repository == null) {
            throw new IllegalStateException("principal refresh token dao 未配置");
        }
        return repository;
    }

    private long refreshTokenTtlSeconds(PrincipalClientId clientId) {
        return 2592000L;
    }

    private User getUser(UserId userId) {
        return userService.get(userId);
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

    private BizException invalidToken() {
        return new BizException("AUTH-00006", "auth.exception.invalid-token", "token 已失效");
    }
}
