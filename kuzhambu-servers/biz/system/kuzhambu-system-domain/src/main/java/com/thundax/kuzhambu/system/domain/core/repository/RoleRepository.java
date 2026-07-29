package com.thundax.kuzhambu.system.domain.core.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.MenuId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.util.List;

public interface RoleRepository {

    Role getById(RoleId id);

    List<Role> listByIds(List<RoleId> idList);

    List<Role> list(String status);

    int maxPriority();

    List<Role> list(SortDirection sortDirection);

    PageResult<Role> page(String status, int pageNo, int pageSize);

    RoleId insert(Role role);

    int update(Role role);

    int updatePriority(Role role);

    int deleteById(RoleId id);

    int updateStatus(Role role);

    List<MenuId> listRoleMenus(RoleId roleId);

    void deleteRoleMenu(RoleId roleId);

    void insertRoleMenu(RoleId roleId, List<MenuId> menuIdList);

    List<UserId> listRoleUsers(RoleId roleId);

    void deleteRoleUser(RoleId roleId);

    void insertRoleUser(RoleId roleId, List<UserId> userIdList);
}
