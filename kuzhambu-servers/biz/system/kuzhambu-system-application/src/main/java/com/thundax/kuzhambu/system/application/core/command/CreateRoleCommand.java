package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.enums.RolePrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.RoleStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import java.util.List;

public record CreateRoleCommand(
        RoleId id, String name, RolePrivilege privilege, RoleStatus status, String remarks, List<MenuId> menuIdList) {}
