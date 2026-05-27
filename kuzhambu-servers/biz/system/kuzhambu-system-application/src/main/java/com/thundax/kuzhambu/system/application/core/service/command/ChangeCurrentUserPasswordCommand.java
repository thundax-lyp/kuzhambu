package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.domain.core.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCurrentUserPasswordCommand {
    private UserId userId;
    private String oldPassword;
    private String password;
}
