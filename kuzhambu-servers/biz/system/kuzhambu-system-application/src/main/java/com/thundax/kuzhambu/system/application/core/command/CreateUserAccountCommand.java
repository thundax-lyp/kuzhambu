package com.thundax.kuzhambu.system.application.core.command;

public record CreateUserAccountCommand(CreateUserCommand userCommand, String loginName, String encryptedPassword) {}
