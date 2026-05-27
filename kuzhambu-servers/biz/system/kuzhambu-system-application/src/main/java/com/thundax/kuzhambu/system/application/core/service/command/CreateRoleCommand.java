package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.enums.RolePrivilege;
import com.thundax.kuzhambu.system.application.core.entity.enums.RoleStatus;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.MenuId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.RoleId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleCommand {
    private RoleId id;
    private String name;
    private RolePrivilege privilege;
    private RoleStatus status;
    private String remarks;
    private List<MenuId> menuIdList;
}
