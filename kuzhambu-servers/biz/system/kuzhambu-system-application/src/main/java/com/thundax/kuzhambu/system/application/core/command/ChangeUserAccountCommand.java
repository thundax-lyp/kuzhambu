package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public record ChangeUserAccountCommand(
        ChangeUserInfoCommand userCommand, UserId userId, String loginName, String encryptedPassword) {}
