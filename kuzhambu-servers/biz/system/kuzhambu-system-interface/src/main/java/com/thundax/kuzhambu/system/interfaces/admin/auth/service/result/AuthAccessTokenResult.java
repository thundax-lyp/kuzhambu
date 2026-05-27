package com.thundax.kuzhambu.system.interfaces.admin.auth.service.result;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthAccessTokenResult {
    private final String token;
    private final String refreshToken;
    private final PrincipalAccessToken principalAccessToken;

    public String getUserId() {
        if (principalAccessToken == null || principalAccessToken.getPrincipalKey() == null) {
            return null;
        }
        return String.valueOf(principalAccessToken.getPrincipalKey().getPrincipalId());
    }
}
