package com.thundax.kuzhambu.biz.core.service;

import com.thundax.kuzhambu.biz.core.entity.Menu;
import com.thundax.kuzhambu.biz.core.entity.Role;
import com.thundax.kuzhambu.biz.core.entity.User;
import com.thundax.kuzhambu.biz.core.entity.valueobject.RoleId;
import com.thundax.kuzhambu.biz.core.service.command.AssignRoleUsersCommand;
import com.thundax.kuzhambu.biz.core.service.command.ChangeRoleInfoCommand;
import com.thundax.kuzhambu.biz.core.service.command.ChangeRoleStatusCommand;
import com.thundax.kuzhambu.biz.core.service.command.CreateRoleCommand;
import com.thundax.kuzhambu.biz.core.service.command.RoleSortCommand;
import com.thundax.kuzhambu.biz.core.service.query.RoleQuery;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
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
