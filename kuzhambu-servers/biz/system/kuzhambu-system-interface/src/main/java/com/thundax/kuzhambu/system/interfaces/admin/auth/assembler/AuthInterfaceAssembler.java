package com.thundax.kuzhambu.system.interfaces.admin.auth.assembler;

import com.thundax.kuzhambu.system.application.auth.entity.PreAuthSession;
import com.thundax.kuzhambu.system.application.auth.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.AuthAccessTokenResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.AuthLoginFormResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.OAuth2AuthorizationDecisionResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.OAuth2AuthorizationViewResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.OAuth2IntrospectionResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.OAuth2UserinfoResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response.TokenVerifyResponse;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthAccessTokenResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenQueryResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.AuthTokenRefreshResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.OAuth2AuthorizationDecisionResult;
import com.thundax.kuzhambu.system.interfaces.admin.auth.service.result.OAuth2AuthorizationViewResult;
import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;
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
    public static AuthAccessTokenResponse toAccessTokenResponse(AuthAccessTokenResult entity) {
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
    public static AuthAccessTokenResponse toAccessTokenResponse(AuthTokenRefreshResult result) {
        if (result == null || result.getAccessToken() == null) {
            return AuthAccessTokenResponse.builder().build();
        }
        return AuthAccessTokenResponse.builder()
                .token(
                        result.getOauthAccessToken() == null
                                ? result.getAccessToken().getToken()
                                : result.getOauthAccessToken())
                .refreshToken(result.getRefreshToken())
                .expireAt(accessTokenExpireAt(result.getAccessToken()))
                .build();
    }

    private static Long accessTokenExpireAt(AuthAccessTokenResult result) {
        return result == null
                        || result.getPrincipalAccessToken() == null
                        || result.getPrincipalAccessToken().getExpireAt() == null
                ? null
                : result.getPrincipalAccessToken().getExpireAt().getTime();
    }

    @NonNull
    public static TokenVerifyResponse toTokenVerifyResponse(AuthTokenQueryResult result) {
        return TokenVerifyResponse.builder()
                .active(result != null && result.isActive())
                .build();
    }

    @NonNull
    public static OAuth2IntrospectionResponse toIntrospectionResponse(AuthTokenQueryResult result) {
        if (result == null || !result.isActive()) {
            return OAuth2IntrospectionResponse.builder().active(false).build();
        }
        OAuth2IntrospectionResponse.OAuth2IntrospectionResponseBuilder builder = OAuth2IntrospectionResponse.builder()
                .active(true)
                .subject(userId(result.getUser()))
                .username(result.getUsername());
        PrincipalAccessToken principalAccessToken = result.getPrincipalAccessToken();
        if (principalAccessToken != null) {
            builder.clientId(principalAccessToken.getClientId())
                    .scope(scope(principalAccessToken.getScopes()))
                    .expiresAt(epochSeconds(principalAccessToken.getExpireAt()))
                    .tokenType("Bearer");
        }
        if (result.getSession() != null) {
            builder.sessionId(result.getSession().getId().value());
        }
        return builder.build();
    }

    @NonNull
    public static OAuth2UserinfoResponse toUserinfoResponse(AuthTokenQueryResult result) {
        if (result == null || !result.isActive()) {
            return OAuth2UserinfoResponse.builder().build();
        }
        return OAuth2UserinfoResponse.builder()
                .subject(userId(result.getUser()))
                .username(result.getUsername())
                .preferredUsername(result.getUsername())
                .name(result.getUser() == null ? null : result.getUser().getName())
                .build();
    }

    @NonNull
    public static OAuth2AuthorizationViewResponse toAuthorizationViewResponse(OAuth2AuthorizationViewResult result) {
        if (result == null) {
            return OAuth2AuthorizationViewResponse.builder().build();
        }
        return OAuth2AuthorizationViewResponse.builder()
                .clientId(result.getClientId())
                .clientName(result.getClientName())
                .redirectUri(result.getRedirectUri())
                .scopes(result.getScopes())
                .state(result.getState())
                .build();
    }

    @NonNull
    public static OAuth2AuthorizationDecisionResponse toAuthorizationDecisionResponse(
            OAuth2AuthorizationDecisionResult result) {
        if (result == null) {
            return OAuth2AuthorizationDecisionResponse.builder().build();
        }
        return OAuth2AuthorizationDecisionResponse.builder()
                .approved(result.isApproved())
                .authorizationCode(result.getAuthorizationCode())
                .state(result.getState())
                .build();
    }

    private static String userId(User user) {
        return user == null || user.getId() == null
                ? null
                : String.valueOf(user.getId().value());
    }

    private static String scope(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(" ");
        for (String scope : scopes) {
            joiner.add(scope);
        }
        return joiner.toString();
    }

    private static Long epochSeconds(Date date) {
        return date == null ? null : date.getTime() / 1000L;
    }
}
