package com.thundax.kuzhambu.system.domain.core.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DepartmentId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.RoleId;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.util.List;

public interface UserRepository {

    User getById(UserId id);

    List<User> listByIds(List<UserId> idList);

    List<User> list(
            DepartmentId departmentId, String loginName, String name, UserStatus status, UserPrivilege privilege);

    PageResult<User> page(
            DepartmentId departmentId,
            String loginName,
            String name,
            UserStatus status,
            UserPrivilege privilege,
            int pageNo,
            int pageSize);

    int countByEmail(String email, UserId excludedId);

    int countByMobile(String mobile, UserId excludedId);

    UserId insert(User user);

    int update(User user);

    int deleteById(UserId id);

    int updateStatus(User user);

    List<RoleId> listUserRoles(UserId userId);

    void deleteUserRole(UserId userId);

    void insertUserRole(UserId userId, List<RoleId> roleIdList);
}
