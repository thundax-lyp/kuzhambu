package com.thundax.kuzhambu.system.domain.core.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.system.domain.core.model.entity.Role;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import java.util.List;

public interface RoleRepository {

    Role getById(RoleId id);

    List<Role> listByIds(List<Long> idList);

    List<Role> list(String status);

    int maxPriority();

    List<Role> list(SortDirection sortDirection);

    Page<Role> page(String status, int pageNo, int pageSize);

    RoleId insert(Role role);

    int update(Role role);

    int updatePriority(Role role);

    int deleteById(RoleId id);

    int updateStatus(Role role);

    List<Long> listRoleMenus(Long roleId);

    void deleteRoleMenu(Long roleId);

    void insertRoleMenu(Long roleId, List<Long> menuIdList);

    List<Long> listRoleUsers(Long roleId);

    void deleteRoleUser(Long roleId);

    void insertRoleUser(Long roleId, List<Long> userIdList);
}
