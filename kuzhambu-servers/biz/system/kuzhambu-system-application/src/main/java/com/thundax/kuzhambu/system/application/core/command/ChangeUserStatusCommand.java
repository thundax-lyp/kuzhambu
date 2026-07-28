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
    private User afterUser;

    public ChangeUserStatusCommand(UserId id, UserStatus status) {
        this(id, status, null);
    }

    public ChangeUserStatusCommand(UserId id, UserStatus status, User beforeUser) {
        this.id = id;
        this.status = status;
        this.beforeUser = beforeUser;
    }
}
