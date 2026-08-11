package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.system.application.core.command.ChangeUserAccountCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateUserAccountCommand;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public interface UserAccountApplicationService {

    UserId create(CreateUserAccountCommand command);

    void change(ChangeUserAccountCommand command);
}
