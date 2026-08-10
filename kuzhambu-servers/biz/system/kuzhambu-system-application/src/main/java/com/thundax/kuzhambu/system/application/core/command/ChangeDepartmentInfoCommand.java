package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;

public record ChangeDepartmentInfoCommand(
        DepartmentId id, DepartmentId parentId, String name, String shortName, String remarks) {}
