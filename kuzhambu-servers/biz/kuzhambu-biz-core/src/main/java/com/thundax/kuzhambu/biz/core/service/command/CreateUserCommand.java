package com.thundax.kuzhambu.biz.core.service.command;

import com.thundax.kuzhambu.biz.core.entity.enums.UserPrivilege;
import com.thundax.kuzhambu.biz.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.biz.core.entity.valueobject.AccessRank;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DepartmentId;
import com.thundax.kuzhambu.biz.core.entity.valueobject.RoleId;
import com.thundax.kuzhambu.biz.core.entity.valueobject.UserId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserCommand {
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
    private String encryptedPassword;
    private List<RoleId> roleIdList;
}
