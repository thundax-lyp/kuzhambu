package com.thundax.kuzhambu.system.application.auth.query;

import com.thundax.kuzhambu.system.domain.auth.model.valueobject.PreAuthSessionId;

public record PreAuthSessionValueValidateQuery(
        PreAuthSessionId id, String name, String value, String bindName, String bindValue) {}
