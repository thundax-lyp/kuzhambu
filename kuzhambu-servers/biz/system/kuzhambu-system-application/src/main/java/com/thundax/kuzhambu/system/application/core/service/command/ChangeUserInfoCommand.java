package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.domain.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.model.valueobject.AccessRank;
import com.thundax.kuzhambu.system.domain.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.model.valueobject.UserId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserInfoCommand {
    private UserId id;
    private DepartmentId departmentId;
    private String email;
    private String mobile;
    private String tel;
    private String name;
    private AccessRank rank;
    private UserPrivilege privilege;
    private UserStatus status;
    private String remarks;
    private String loginName;
    private List<RoleId> roleIdList;
}
