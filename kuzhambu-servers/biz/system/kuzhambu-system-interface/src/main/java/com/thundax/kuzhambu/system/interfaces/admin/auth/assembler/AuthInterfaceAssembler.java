package com.thundax.kuzhambu.system.interfaces.admin.auth.assembler;

import com.thundax.kuzhambu.system.application.auth.command.AuthenticateIdentityCommand;
import com.thundax.kuzhambu.system.application.auth.command.AuthenticatePasswordCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreateAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.command.CreatePreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.DeleteAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.command.InvalidateAdminSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.RecordPrincipalLoginFailureCommand;
import com.thundax.kuzhambu.system.application.auth.command.RefreshAdminAccessTokenCommand;
import com.thundax.kuzhambu.system.application.auth.command.RefreshPreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.ReleasePreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.UpsertPreAuthSessionValueCommand;
import com.thundax.kuzhambu.system.application.auth.query.AdminAccessTokenQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueValidateQuery;
import com.thundax.kuzhambu.system.application.auth.service.dto.PrincipalPasswordPolicyDTO;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PreAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalAuthenticationMethod;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalCredentialType;
import com.thundax.kuzhambu.system.domain.auth.model.enums.PrincipalIdentityType;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalClientId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalKey;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.AuthAccessTokenResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.AuthLoginFormResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.TokenVerifyResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthAccessTokenDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenQueryDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto.AuthTokenRefreshDTO;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.support.AdminAuthOperation;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class AuthInterfaceAssembler {
    private static final String PUBLIC_KEY_ITEM = "publicKey";

    private AuthInterfaceAssembler() {}

    @NonNull
    public static CreateAdminAccessTokenCommand toCreateAdminAccessTokenCommand(
            @NonNull UserId userId,
            @NonNull String loginName,
            @NonNull String ip,
            @NonNull String userAgent,
            @NonNull PrincipalAuthenticationMethod authenticationMethod,
            @NonNull PrincipalIdentityType identityType) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(loginName, "loginName must not be null");
        Objects.requireNonNull(ip, "ip must not be null");
        Objects.requireNonNull(userAgent, "userAgent must not be null");
        Objects.requireNonNull(authenticationMethod, "authenticationMethod must not be null");
        Objects.requireNonNull(identityType, "identityType must not be null");
        return new CreateAdminAccessTokenCommand(userId, loginName, ip, userAgent, authenticationMethod, identityType);
    }

    @NonNull
    public static CreateAdminAccessTokenCommand toCreateAdminAccessTokenCommand(
            @NonNull AdminAuthOperation operation,
            @NonNull PrincipalAuthenticationMethod authenticationMethod,
            @NonNull PrincipalIdentityType identityType) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(operation.getUserId(), "operation.userId must not be null");
        Objects.requireNonNull(operation.getLoginName(), "operation.loginName must not be null");
        Objects.requireNonNull(authenticationMethod, "authenticationMethod must not be null");
        Objects.requireNonNull(identityType, "identityType must not be null");
        return new CreateAdminAccessTokenCommand(
                operation.getUserId(),
                operation.getLoginName(),
                operation.getIp(),
                operation.getUserAgent(),
                authenticationMethod,
                identityType);
    }

    @NonNull
    public static DeleteAdminAccessTokenCommand toDeleteAdminAccessTokenCommand(
            @NonNull PrincipalAccessTokenCode token, @NonNull String ip, @NonNull String userAgent) {
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(ip, "ip must not be null");
        Objects.requireNonNull(userAgent, "userAgent must not be null");
        return new DeleteAdminAccessTokenCommand(token, null, ip, userAgent);
    }

    @NonNull
    public static DeleteAdminAccessTokenCommand toDeleteAdminAccessTokensByUserIdCommand(
            @NonNull AdminAuthOperation operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(operation.getUserId(), "operation.userId must not be null");
        return new DeleteAdminAccessTokenCommand(null, operation.getUserId(), null, null);
    }

    @NonNull
    public static DeleteAdminAccessTokenCommand toDeleteAdminAccessTokenCommand(
            @NonNull AdminAuthOperation operation, @NonNull PrincipalAccessTokenCode token) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(token, "token must not be null");
        return new DeleteAdminAccessTokenCommand(token, null, operation.getIp(), operation.getUserAgent());
    }

    private static DeleteAdminAccessTokenCommand deleteAdminAccessTokenCommand(
            PrincipalAccessTokenCode token, UserId userId, String ip, String userAgent) {
        return new DeleteAdminAccessTokenCommand(token, userId, ip, userAgent);
    }

    @NonNull
    public static InvalidateAdminSessionCommand toInvalidateAdminSessionCommand(
            @NonNull PrincipalAccessTokenCode token, @NonNull String reason) {
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        return new InvalidateAdminSessionCommand(token, null, reason);
    }

    @NonNull
    public static InvalidateAdminSessionCommand toInvalidateSessionByTokenCommand(
            @NonNull AdminAuthOperation operation, @NonNull PrincipalAccessTokenCode token) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(token, "token must not be null");
        return new InvalidateAdminSessionCommand(token, null, operation.getReason());
    }

    @NonNull
    public static InvalidateAdminSessionCommand toInvalidateSessionsByUserIdCommand(
            @NonNull AdminAuthOperation operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(operation.getUserId(), "operation.userId must not be null");
        return new InvalidateAdminSessionCommand(null, operation.getUserId(), operation.getReason());
    }

    private static InvalidateAdminSessionCommand invalidateAdminSessionCommand(
            PrincipalAccessTokenCode token, UserId userId, String reason) {
        return new InvalidateAdminSessionCommand(token, userId, reason);
    }

    @NonNull
    public static RecordPrincipalLoginFailureCommand toRecordPrincipalLoginFailureCommand(
            @NonNull PrincipalKey principalKey,
            @NonNull PrincipalAuthenticationMethod authenticationMethod,
            @NonNull PrincipalIdentityType identityType,
            @NonNull String ip,
            @NonNull String userAgent,
            @NonNull String reason) {
        Objects.requireNonNull(principalKey, "principalKey must not be null");
        Objects.requireNonNull(authenticationMethod, "authenticationMethod must not be null");
        Objects.requireNonNull(identityType, "identityType must not be null");
        Objects.requireNonNull(ip, "ip must not be null");
        Objects.requireNonNull(userAgent, "userAgent must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        return new RecordPrincipalLoginFailureCommand(
                principalKey, authenticationMethod, identityType, ip, userAgent, reason);
    }

    @NonNull
    public static RecordPrincipalLoginFailureCommand toRecordPrincipalLoginFailureCommand(
            @NonNull AdminAuthOperation operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(operation.getAuthenticationMethod(), "operation.authenticationMethod must not be null");
        Objects.requireNonNull(operation.getIdentityType(), "operation.identityType must not be null");
        Objects.requireNonNull(operation.getReason(), "operation.reason must not be null");
        return new RecordPrincipalLoginFailureCommand(
                null,
                operation.getAuthenticationMethod(),
                operation.getIdentityType(),
                operation.getIp(),
                operation.getUserAgent(),
                operation.getReason());
    }

    @NonNull
    public static RecordPrincipalLoginFailureCommand toRecordPrincipalLoginFailureCommand(
            @NonNull PrincipalKey principalKey, @NonNull AdminAuthOperation operation, @NonNull String reason) {
        Objects.requireNonNull(principalKey, "principalKey must not be null");
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(operation.getAuthenticationMethod(), "operation.authenticationMethod must not be null");
        Objects.requireNonNull(operation.getIdentityType(), "operation.identityType must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        return new RecordPrincipalLoginFailureCommand(
                principalKey,
                operation.getAuthenticationMethod(),
                operation.getIdentityType(),
                operation.getIp(),
                operation.getUserAgent(),
                reason);
    }

    @NonNull
    public static RefreshAdminAccessTokenCommand toRefreshAdminAccessTokenCommand(
            @NonNull PrincipalClientId clientId,
            @NonNull PrincipalRefreshTokenCode refreshToken,
            @NonNull String ip,
            @NonNull String userAgent) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        Objects.requireNonNull(ip, "ip must not be null");
        Objects.requireNonNull(userAgent, "userAgent must not be null");
        return new RefreshAdminAccessTokenCommand(clientId, refreshToken, ip, userAgent);
    }

    @NonNull
    public static RefreshAdminAccessTokenCommand toRefreshAdminAccessTokenCommand(
            @NonNull AdminAuthOperation operation,
            @NonNull PrincipalClientId clientId,
            @NonNull PrincipalRefreshTokenCode refreshToken) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        return new RefreshAdminAccessTokenCommand(clientId, refreshToken, operation.getIp(), operation.getUserAgent());
    }

    @NonNull
    public static AdminAccessTokenQuery toAdminAccessTokenQuery(@NonNull PrincipalAccessTokenCode token) {
        Objects.requireNonNull(token, "token must not be null");
        return new AdminAccessTokenQuery(token);
    }

    @NonNull
    public static AuthenticatePasswordCommand toAuthenticatePasswordCommand(
            @NonNull PrincipalIdentityType identityType,
            @NonNull String identityValue,
            @NonNull PrincipalCredentialType credentialType,
            @NonNull String plainPassword,
            @NonNull PrincipalPasswordPolicyDTO passwordPolicy) {
        Objects.requireNonNull(identityType, "identityType must not be null");
        Objects.requireNonNull(identityValue, "identityValue must not be null");
        Objects.requireNonNull(credentialType, "credentialType must not be null");
        Objects.requireNonNull(plainPassword, "plainPassword must not be null");
        Objects.requireNonNull(passwordPolicy, "passwordPolicy must not be null");
        return new AuthenticatePasswordCommand(
                identityType, identityValue, credentialType, plainPassword, passwordPolicy);
    }

    @NonNull
    public static AuthenticateIdentityCommand toAuthenticateIdentityCommand(
            @NonNull PrincipalIdentityType identityType, @NonNull String identityValue) {
        Objects.requireNonNull(identityType, "identityType must not be null");
        Objects.requireNonNull(identityValue, "identityValue must not be null");
        return new AuthenticateIdentityCommand(identityType, identityValue);
    }

    @NonNull
    public static CreatePreAuthSessionCommand toCreatePreAuthSessionCommand(int expiredSeconds) {
        return new CreatePreAuthSessionCommand(expiredSeconds);
    }

    @NonNull
    public static RefreshPreAuthSessionCommand toRefreshPreAuthSessionCommand(
            @NonNull PreAuthSessionId id, int expiredSeconds, int refreshTokenGraceSeconds) {
        Objects.requireNonNull(id, "id must not be null");
        return new RefreshPreAuthSessionCommand(id, expiredSeconds, refreshTokenGraceSeconds);
    }

    @NonNull
    public static ReleasePreAuthSessionCommand toReleasePreAuthSessionCommand(@NonNull PreAuthSessionId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new ReleasePreAuthSessionCommand(id);
    }

    @NonNull
    public static UpsertPreAuthSessionValueCommand toUpsertPreAuthSessionValueCommand(
            @NonNull PreAuthSessionId id, @NonNull String name, @NonNull String value, long expiredAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        return new UpsertPreAuthSessionValueCommand(id, name, value, expiredAt);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionQuery(@NonNull PreAuthSessionId id) {
        Objects.requireNonNull(id, "id must not be null");
        return new PreAuthSessionQuery(id, null, null);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionTokenQuery(@NonNull String token) {
        Objects.requireNonNull(token, "token must not be null");
        return new PreAuthSessionQuery(null, PreAuthSessionToken.of(token), null);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionRefreshTokenQuery(@NonNull String refreshToken) {
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        return new PreAuthSessionQuery(null, null, PreAuthSessionToken.of(refreshToken));
    }

    @NonNull
    public static PreAuthSessionValueQuery toPreAuthSessionValueQuery(
            @NonNull PreAuthSessionId id, @NonNull String name) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        return new PreAuthSessionValueQuery(id, name);
    }

    @NonNull
    public static PreAuthSessionValueValidateQuery toPreAuthSessionValueValidateQuery(
            @NonNull PreAuthSessionId id, @NonNull String name, @NonNull String value) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        return new PreAuthSessionValueValidateQuery(id, name, value, null, null);
    }

    @NonNull
    public static PreAuthSessionValueValidateQuery toPreAuthSessionValueValidateQuery(
            @NonNull PreAuthSessionId id,
            @NonNull String name,
            @NonNull String value,
            @NonNull String bindName,
            @NonNull String bindValue) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(bindName, "bindName must not be null");
        Objects.requireNonNull(bindValue, "bindValue must not be null");
        return new PreAuthSessionValueValidateQuery(id, name, value, bindName, bindValue);
    }

    @NonNull
    public static AuthLoginFormResponse emptyLoginFormResponse() {
        return AuthLoginFormResponse.builder().build();
    }

    @NonNull
    public static AuthLoginFormResponse toLoginFormResponse(@NonNull PreAuthSession session) {
        Objects.requireNonNull(session, "session must not be null");
        return AuthLoginFormResponse.builder()
                .loginToken(session.getToken().asString())
                .refreshToken(session.getRefreshToken().asString())
                .expiredAt(session.getExpiredAt())
                .publicKey(session.findValue(PUBLIC_KEY_ITEM))
                .build();
    }

    @NonNull
    public static AuthAccessTokenResponse emptyAccessTokenResponse() {
        return AuthAccessTokenResponse.builder().build();
    }

    @NonNull
    public static AuthAccessTokenResponse toAccessTokenResponse(@NonNull AuthAccessTokenDTO entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return AuthAccessTokenResponse.builder()
                .token(entity.getToken())
                .refreshToken(entity.getRefreshToken())
                .expireAt(accessTokenExpireAt(entity))
                .build();
    }

    @NonNull
    public static AuthAccessTokenResponse toAccessTokenResponse(@NonNull AuthTokenRefreshDTO result) {
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(result.getAccessToken(), "result.accessToken must not be null");
        return AuthAccessTokenResponse.builder()
                .token(result.getAccessToken().getToken())
                .refreshToken(result.getRefreshToken())
                .expireAt(accessTokenExpireAt(result.getAccessToken()))
                .build();
    }

    private static Long accessTokenExpireAt(AuthAccessTokenDTO result) {
        return result == null
                        || result.getPrincipalAccessToken() == null
                        || result.getPrincipalAccessToken().getExpireAt() == null
                ? null
                : result.getPrincipalAccessToken().getExpireAt().toEpochMilli();
    }

    @NonNull
    public static TokenVerifyResponse inactiveTokenVerifyResponse() {
        return TokenVerifyResponse.builder().active(false).build();
    }

    @NonNull
    public static TokenVerifyResponse toTokenVerifyResponse(@NonNull AuthTokenQueryDTO result) {
        Objects.requireNonNull(result, "result must not be null");
        return TokenVerifyResponse.builder().active(result.isActive()).build();
    }
}
