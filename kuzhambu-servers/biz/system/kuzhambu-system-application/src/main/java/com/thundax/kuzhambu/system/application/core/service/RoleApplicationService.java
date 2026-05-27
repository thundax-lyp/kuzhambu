package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.AssignRoleUsersCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeRoleInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeRoleStatusCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateRoleCommand;
import com.thundax.kuzhambu.system.application.core.command.RoleSortCommand;
import com.thundax.kuzhambu.system.application.core.query.RoleQuery;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import java.util.List;

public interface RoleApplicationService {

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
