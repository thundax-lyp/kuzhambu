package com.thundax.kuzhambu.system.application.auth.exception;

import com.thundax.kuzhambu.common.core.exception.BizException;

public class InvalidPasswordException extends BizException {

    public InvalidPasswordException() {
        super("AUTH-00003", "auth.exception.invalid-password", "密码错误");
    }
}
