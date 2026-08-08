package com.thundax.kuzhambu.system.interfaces.admin.auth.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenRefreshDTO {
    private AuthAccessTokenDTO accessToken;
    private String refreshToken;

    public AuthTokenRefreshDTO(AuthAccessTokenDTO accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
