package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.application.core.entity.enums.UserStatus;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserStatusCommand {
    private UserId id;
    private UserStatus status;
}
