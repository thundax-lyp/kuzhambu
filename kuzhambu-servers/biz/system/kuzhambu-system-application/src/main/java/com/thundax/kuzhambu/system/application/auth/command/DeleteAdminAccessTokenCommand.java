package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public record DeleteAdminAccessTokenCommand(
        PrincipalAccessTokenCode token, UserId userId, String ip, String userAgent) {}
