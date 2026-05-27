package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.entity.Menu;
import com.thundax.kuzhambu.system.application.core.entity.Role;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.service.command.AssignRoleUsersCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeRoleInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeRoleStatusCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateRoleCommand;
import com.thundax.kuzhambu.system.application.core.service.command.RoleSortCommand;
import com.thundax.kuzhambu.system.application.core.service.query.RoleQuery;
import com.thundax.kuzhambu.system.domain.model.valueobject.RoleId;
import java.util.List;

public interface RoleService {

    Role get(RoleId id);

    List<Role> list(RoleQuery query);

    PageResult<Role> page(RoleQuery query, PageQuery page);

    RoleId create(CreateRoleCommand command);

    void changeInfo(ChangeRoleInfoCommand command);

    int remove(RoleId id);

    void sort(RoleSortCommand command);

    int changeStatus(ChangeRoleStatusCommand command);

    void assignUsers(AssignRoleUsersCommand command);

    List<User> listRoleUsers(RoleQuery query);

    List<Menu> listRoleMenus(RoleQuery query);
}
