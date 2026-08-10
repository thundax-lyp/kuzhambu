package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public record ChangeCurrentUserInfoCommand(
        UserId userId,
        DepartmentId departmentId,
        String email,
        String mobile,
        String tel,
        String name,
        AccessRank rank,
        UserPrivilege privilege,
        UserStatus status,
        String remarks) {}
