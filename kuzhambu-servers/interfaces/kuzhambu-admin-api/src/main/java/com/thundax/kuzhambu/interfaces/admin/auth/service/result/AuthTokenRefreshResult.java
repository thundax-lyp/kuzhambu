package com.thundax.kuzhambu.interfaces.admin.auth.service.result;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenRefreshResult {
    private AuthAccessTokenResult accessToken;
    private String refreshToken;
    private String oauthAccessToken;

    public AuthTokenRefreshResult(AuthAccessTokenResult accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public AuthTokenRefreshResult(AuthAccessTokenResult accessToken, String refreshToken, String oauthAccessToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.oauthAccessToken = oauthAccessToken;
    }
}
