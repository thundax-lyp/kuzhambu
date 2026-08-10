package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PrincipalAccessTokenCode;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public record CreatePermissionsCommand(PrincipalAccessTokenCode token, UserId userId) {}
