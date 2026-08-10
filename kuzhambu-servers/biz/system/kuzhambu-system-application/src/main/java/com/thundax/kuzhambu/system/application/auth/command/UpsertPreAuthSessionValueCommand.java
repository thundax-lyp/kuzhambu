package com.thundax.kuzhambu.system.application.auth.command;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;

public record UpsertPreAuthSessionValueCommand(PreAuthSessionId id, String name, String value, long expiredAt) {}
