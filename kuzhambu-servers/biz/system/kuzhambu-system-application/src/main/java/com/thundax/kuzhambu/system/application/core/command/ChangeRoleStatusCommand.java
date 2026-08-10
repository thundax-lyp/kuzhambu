package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.enums.RoleStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;

public record ChangeRoleStatusCommand(RoleId id, RoleStatus status) {}
