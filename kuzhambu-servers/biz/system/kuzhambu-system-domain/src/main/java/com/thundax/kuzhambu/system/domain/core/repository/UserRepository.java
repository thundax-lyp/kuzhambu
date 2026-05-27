package com.thundax.kuzhambu.system.domain.core.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserPrivilege;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.util.List;

public interface UserRepository {

    User getById(UserId id);

    List<User> listByIds(List<Long> idList);

    List<User> list(Long departmentId, String loginName, String name, UserStatus status, UserPrivilege privilege);

    PageResult<User> page(
            Long departmentId,
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

    List<Long> listUserRoles(Long userId);

    void deleteUserRole(Long userId);

    void insertUserRole(Long userId, List<Long> roleIdList);
}
