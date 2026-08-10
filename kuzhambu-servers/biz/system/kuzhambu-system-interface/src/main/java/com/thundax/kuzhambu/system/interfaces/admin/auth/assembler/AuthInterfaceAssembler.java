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
import org.springframework.lang.NonNull;

public final class AuthInterfaceAssembler {
    private static final String PUBLIC_KEY_ITEM = "publicKey";

    private AuthInterfaceAssembler() {}

    @NonNull
    public static CreateAdminAccessTokenCommand toCreateAdminAccessTokenCommand(
            UserId userId,
            String loginName,
            String ip,
            String userAgent,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType) {
        return new CreateAdminAccessTokenCommand(userId, loginName, ip, userAgent, authenticationMethod, identityType);
    }

    @NonNull
    public static DeleteAdminAccessTokenCommand toDeleteAdminAccessTokenCommand(
            PrincipalAccessTokenCode token, UserId userId, String ip, String userAgent) {
        return new DeleteAdminAccessTokenCommand(token, userId, ip, userAgent);
    }

    @NonNull
    public static InvalidateAdminSessionCommand toInvalidateAdminSessionCommand(
            PrincipalAccessTokenCode token, UserId userId, String reason) {
        return new InvalidateAdminSessionCommand(token, userId, reason);
    }

    @NonNull
    public static RecordPrincipalLoginFailureCommand toRecordPrincipalLoginFailureCommand(
            PrincipalKey principalKey,
            PrincipalAuthenticationMethod authenticationMethod,
            PrincipalIdentityType identityType,
            String ip,
            String userAgent,
            String reason) {
        return new RecordPrincipalLoginFailureCommand(
                principalKey, authenticationMethod, identityType, ip, userAgent, reason);
    }

    @NonNull
    public static RefreshAdminAccessTokenCommand toRefreshAdminAccessTokenCommand(
            PrincipalClientId clientId, PrincipalRefreshTokenCode refreshToken, String ip, String userAgent) {
        return new RefreshAdminAccessTokenCommand(clientId, refreshToken, ip, userAgent);
    }

    @NonNull
    public static AdminAccessTokenQuery toAdminAccessTokenQuery(PrincipalAccessTokenCode token) {
        return new AdminAccessTokenQuery(token);
    }

    @NonNull
    public static AuthenticatePasswordCommand toAuthenticatePasswordCommand(
            PrincipalIdentityType identityType,
            String identityValue,
            PrincipalCredentialType credentialType,
            String plainPassword,
            PrincipalPasswordPolicyDTO passwordPolicy) {
        return new AuthenticatePasswordCommand(
                identityType, identityValue, credentialType, plainPassword, passwordPolicy);
    }

    @NonNull
    public static AuthenticateIdentityCommand toAuthenticateIdentityCommand(
            PrincipalIdentityType identityType, String identityValue) {
        return new AuthenticateIdentityCommand(identityType, identityValue);
    }

    @NonNull
    public static CreatePreAuthSessionCommand toCreatePreAuthSessionCommand(int expiredSeconds) {
        return new CreatePreAuthSessionCommand(expiredSeconds);
    }

    @NonNull
    public static RefreshPreAuthSessionCommand toRefreshPreAuthSessionCommand(
            PreAuthSessionId id, int expiredSeconds, int refreshTokenGraceSeconds) {
        return new RefreshPreAuthSessionCommand(id, expiredSeconds, refreshTokenGraceSeconds);
    }

    @NonNull
    public static ReleasePreAuthSessionCommand toReleasePreAuthSessionCommand(PreAuthSessionId id) {
        return new ReleasePreAuthSessionCommand(id);
    }

    @NonNull
    public static UpsertPreAuthSessionValueCommand toUpsertPreAuthSessionValueCommand(
            PreAuthSessionId id, String name, String value, long expiredAt) {
        return new UpsertPreAuthSessionValueCommand(id, name, value, expiredAt);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionQuery(PreAuthSessionId id) {
        return new PreAuthSessionQuery(id, null, null);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionTokenQuery(String token) {
        return new PreAuthSessionQuery(null, PreAuthSessionToken.of(token), null);
    }

    @NonNull
    public static PreAuthSessionQuery toPreAuthSessionRefreshTokenQuery(String refreshToken) {
        return new PreAuthSessionQuery(null, null, PreAuthSessionToken.of(refreshToken));
    }

    @NonNull
    public static PreAuthSessionValueQuery toPreAuthSessionValueQuery(PreAuthSessionId id, String name) {
        return new PreAuthSessionValueQuery(id, name);
    }

    @NonNull
    public static PreAuthSessionValueValidateQuery toPreAuthSessionValueValidateQuery(
            PreAuthSessionId id, String name, String value) {
        return new PreAuthSessionValueValidateQuery(id, name, value, null, null);
    }

    @NonNull
    public static PreAuthSessionValueValidateQuery toPreAuthSessionValueValidateQuery(
            PreAuthSessionId id, String name, String value, String bindName, String bindValue) {
        return new PreAuthSessionValueValidateQuery(id, name, value, bindName, bindValue);
    }

    @NonNull
    public static AuthLoginFormResponse toLoginFormResponse(PreAuthSession session) {
        if (session == null) {
            return AuthLoginFormResponse.builder().build();
        }
        return AuthLoginFormResponse.builder()
                .loginToken(session.getToken().asString())
                .refreshToken(session.getRefreshToken().asString())
                .expiredAt(session.getExpiredAt())
                .publicKey(session.findValue(PUBLIC_KEY_ITEM))
                .build();
    }

    @NonNull
    public static AuthAccessTokenResponse toAccessTokenResponse(AuthAccessTokenDTO entity) {
        if (entity == null) {
            return AuthAccessTokenResponse.builder().build();
        }
        return AuthAccessTokenResponse.builder()
                .token(entity.getToken())
                .refreshToken(entity.getRefreshToken())
                .expireAt(accessTokenExpireAt(entity))
                .build();
    }

    @NonNull
    public static AuthAccessTokenResponse toAccessTokenResponse(AuthTokenRefreshDTO result) {
        if (result == null || result.getAccessToken() == null) {
            return AuthAccessTokenResponse.builder().build();
        }
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
    public static TokenVerifyResponse toTokenVerifyResponse(AuthTokenQueryDTO result) {
        return TokenVerifyResponse.builder()
                .active(result != null && result.isActive())
                .build();
    }
}
