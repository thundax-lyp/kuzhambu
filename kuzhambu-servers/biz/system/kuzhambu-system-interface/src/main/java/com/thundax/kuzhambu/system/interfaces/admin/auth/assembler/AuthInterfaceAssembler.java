package com.thundax.kuzhambu.system.interfaces.admin.auth.assembler;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PreAuthSession;
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
