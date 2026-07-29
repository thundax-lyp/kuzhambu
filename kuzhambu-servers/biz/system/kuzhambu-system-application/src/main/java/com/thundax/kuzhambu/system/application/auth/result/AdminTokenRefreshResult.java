package com.thundax.kuzhambu.system.application.auth.result;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminTokenRefreshResult {
    private AdminAccessTokenResult accessToken;
    private PrincipalRefreshTokenCode refreshToken;

    public AdminTokenRefreshResult(AdminAccessTokenResult accessToken, PrincipalRefreshTokenCode refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
