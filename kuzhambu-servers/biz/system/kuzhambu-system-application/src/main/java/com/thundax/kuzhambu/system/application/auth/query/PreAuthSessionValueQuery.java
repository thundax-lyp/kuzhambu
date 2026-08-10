package com.thundax.kuzhambu.system.application.auth.query;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;

public record PreAuthSessionValueQuery(PreAuthSessionId id, String name) {}
