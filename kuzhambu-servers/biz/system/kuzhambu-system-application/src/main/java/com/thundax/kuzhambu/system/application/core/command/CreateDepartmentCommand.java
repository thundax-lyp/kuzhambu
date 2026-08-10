package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;

public record CreateDepartmentCommand(
        DepartmentId id, DepartmentId parentId, String name, String shortName, String remarks) {}
