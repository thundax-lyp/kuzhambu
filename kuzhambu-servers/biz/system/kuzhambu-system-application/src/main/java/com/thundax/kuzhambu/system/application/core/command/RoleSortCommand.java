package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import java.util.List;

public record RoleSortCommand(List<RoleId> orderedIds) {}
