package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public record UserQuery(
        UserId id,
        DepartmentId departmentId,
        String loginName,
        String email,
        String mobile,
        String name,
        UserStatus status,
        UserPrivilege privilege,
        String orderBy,
        UserId excludedId) {}
