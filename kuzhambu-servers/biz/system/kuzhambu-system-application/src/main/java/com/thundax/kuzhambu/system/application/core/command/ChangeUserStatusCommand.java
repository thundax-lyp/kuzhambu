package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.enums.UserStatus;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangeUserStatusCommand {
    private UserId id;
    private UserStatus status;
    private User beforeUser;

    public ChangeUserStatusCommand(UserId id, UserStatus status) {
        this(id, status, null);
    }

    public ChangeUserStatusCommand(UserId id, UserStatus status, User beforeUser) {
        this.id = id;
        this.status = status;
        this.beforeUser = beforeUser;
    }

    public User auditAfterUser() {
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
