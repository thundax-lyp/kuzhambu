package com.thundax.kuzhambu.system.application.core.service.query;

import com.thundax.kuzhambu.system.application.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.system.application.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery {
    private UserId id;
    private DepartmentId departmentId;
    private String loginName;
    private String email;
    private String mobile;
    private String name;
    private UserStatus status;
    private UserPrivilege privilege;
    private String orderBy;
    private UserId excludedId;
}
