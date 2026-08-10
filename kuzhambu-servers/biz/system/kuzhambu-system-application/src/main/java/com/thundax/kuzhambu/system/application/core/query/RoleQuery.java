package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.enums.RoleStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;

public record RoleQuery(RoleId id, RoleStatus status) {}
