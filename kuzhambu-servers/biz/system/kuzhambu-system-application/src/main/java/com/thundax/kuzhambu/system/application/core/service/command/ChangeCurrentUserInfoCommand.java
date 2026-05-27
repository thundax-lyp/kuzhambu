package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.core.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCurrentUserInfoCommand {
    private UserId userId;
    private DepartmentId departmentId;
    private String email;
    private String mobile;
    private String tel;
    private String name;
    private AccessRank rank;
    private UserPrivilege privilege;
    private UserStatus status;
    private String remarks;
}
