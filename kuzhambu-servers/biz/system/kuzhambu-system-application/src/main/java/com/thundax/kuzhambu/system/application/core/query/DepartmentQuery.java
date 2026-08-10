package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;

public record DepartmentQuery(
        DepartmentId childId, DepartmentId ancestorId, DepartmentId parentId, String name, String remarks) {}
