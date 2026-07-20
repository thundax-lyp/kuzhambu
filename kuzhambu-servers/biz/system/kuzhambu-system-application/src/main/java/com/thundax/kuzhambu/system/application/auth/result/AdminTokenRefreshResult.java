package com.thundax.kuzhambu.system.application.auth.result;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminTokenRefreshResult {
    private AdminAccessTokenResult accessToken;
    private String refreshToken;

    public AdminTokenRefreshResult(AdminAccessTokenResult accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
