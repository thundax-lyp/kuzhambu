package com.thundax.kuzhambu.system.application.auth.query;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;
import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionToken;

public record PreAuthSessionQuery(PreAuthSessionId id, PreAuthSessionToken token, PreAuthSessionToken refreshToken) {}
