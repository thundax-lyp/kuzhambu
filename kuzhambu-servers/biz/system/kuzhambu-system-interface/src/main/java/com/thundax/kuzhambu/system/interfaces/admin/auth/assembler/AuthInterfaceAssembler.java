package com.thundax.kuzhambu.system.interfaces.admin.auth.assembler;

import com.thundax.kuzhambu.system.application.auth.command.CreatePreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.RefreshPreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.ReleasePreAuthSessionCommand;
import com.thundax.kuzhambu.system.application.auth.command.UpsertPreAuthSessionValueCommand;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery;
import com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueValidateQuery;
import com.thundax.kuzhambu.system.domain.auth.model.entity.PreAuthSession;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;
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
