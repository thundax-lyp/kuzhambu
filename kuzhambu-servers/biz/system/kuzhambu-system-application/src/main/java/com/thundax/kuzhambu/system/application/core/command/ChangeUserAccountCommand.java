package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.util.Optional;

public record ChangeUserAccountCommand(
        ChangeUserInfoCommand userCommand, UserId userId, String loginName, Optional<String> encryptedPassword) {}
