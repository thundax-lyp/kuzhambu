package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;

public record RefreshPreAuthSessionCommand(PreAuthSessionId id, int expiredSeconds, int refreshTokenGraceSeconds) {}
