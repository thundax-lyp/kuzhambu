package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public record ChangeUserStatusCommand(UserId id, UserStatus status, User beforeUser, User afterUser) {
    public ChangeUserStatusCommand(UserId id, UserStatus status) {
        this(id, status, null);
    }

    public ChangeUserStatusCommand(UserId id, UserStatus status, User beforeUser) {
        this(id, status, beforeUser, auditAfterUser(id, status, beforeUser));
    }

    private static User auditAfterUser(UserId id, UserStatus status, User beforeUser) {
        User user = new User();
        if (beforeUser != null) {
            user.setId(beforeUser.getId());
            user.setName(beforeUser.getName());
            user.setPrivilege(beforeUser.getPrivilege());
        } else {
            user.setId(id);
        }
        user.setStatus(status);
        return user;
    }
}
