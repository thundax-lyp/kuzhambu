package com.thundax.kuzhambu.biz.core.service;

import com.thundax.kuzhambu.biz.core.entity.Role;
import com.thundax.kuzhambu.biz.core.entity.User;
import com.thundax.kuzhambu.biz.core.entity.valueobject.UserId;
import com.thundax.kuzhambu.biz.core.service.command.ChangeUserInfoCommand;
import com.thundax.kuzhambu.biz.core.service.command.ChangeUserStatusCommand;
import com.thundax.kuzhambu.biz.core.service.command.CreateUserCommand;
import com.thundax.kuzhambu.biz.core.service.query.UserQuery;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
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
