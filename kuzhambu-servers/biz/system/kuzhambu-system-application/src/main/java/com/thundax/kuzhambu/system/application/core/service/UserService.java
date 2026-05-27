package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeUserStatusCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateUserCommand;
import com.thundax.kuzhambu.system.application.core.service.query.UserQuery;
import com.thundax.kuzhambu.system.domain.model.entity.Role;
import com.thundax.kuzhambu.system.domain.model.entity.User;
import com.thundax.kuzhambu.system.domain.model.valueobject.UserId;
import java.util.List;

public interface UserService {

    User get(UserId id);

    List<User> list(UserQuery query);

    PageResult<User> page(UserQuery query, PageQuery page);

    boolean existsEmail(UserQuery query);

    boolean existsMobile(UserQuery query);

    UserId create(CreateUserCommand command);

    void changeInfo(ChangeUserInfoCommand command);

    int remove(UserId id);

    int changeStatus(ChangeUserStatusCommand command);

    List<Role> listUserRoles(UserQuery query);
}
