package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.util.List;

public record ChangeUserInfoCommand(
        UserId id,
        DepartmentId departmentId,
        String email,
        String mobile,
        String tel,
        String name,
        AccessRank rank,
        UserPrivilege privilege,
        UserStatus status,
        String remarks,
        String loginName,
        List<RoleId> roleIdList) {}
