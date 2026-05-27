package com.thundax.kuzhambu.system.interfaces.admin.auth.service.result;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenRefreshResult {
    private AuthAccessTokenResult accessToken;
    private String refreshToken;

    public AuthTokenRefreshResult(AuthAccessTokenResult accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
