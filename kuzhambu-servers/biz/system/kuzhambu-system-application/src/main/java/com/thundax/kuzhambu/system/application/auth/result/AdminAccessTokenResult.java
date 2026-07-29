package com.thundax.kuzhambu.system.application.auth.result;

import com.thundax.kuzhambu.system.domain.auth.model.entity.PrincipalAccessToken;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminAccessTokenResult {
    private final PrincipalAccessTokenCode token;
    private final PrincipalRefreshTokenCode refreshToken;
    private final PrincipalAccessToken principalAccessToken;

    public String getUserId() {
        if (principalAccessToken == null || principalAccessToken.getPrincipalKey() == null) {
            return null;
        }
        return String.valueOf(principalAccessToken.getPrincipalKey().getPrincipalId());
    }
}
