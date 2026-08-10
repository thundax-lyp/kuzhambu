package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalClientId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalRefreshTokenCode;

public record RefreshAdminAccessTokenCommand(
        PrincipalClientId clientId, PrincipalRefreshTokenCode refreshToken, String ip, String userAgent) {}
