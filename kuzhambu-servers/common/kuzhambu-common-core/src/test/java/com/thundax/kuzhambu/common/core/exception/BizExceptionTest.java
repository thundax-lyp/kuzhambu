package com.thundax.kuzhambu.common.core.exception;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class BizExceptionTest {

    @Test
    public void shouldKeepPlainMessageCompatible() {
        BizException exception = new BizException("plain message");

        assertNull(exception.getCode());
        assertNull(exception.getMessageKey());
        assertEquals("plain message", exception.getDefaultMessage());
        assertEquals("plain message", exception.getMessage());
        assertArrayEquals(new Object[0], exception.getMessageArgs());
    }

    @Test
    public void shouldCarryBusinessCodeAndMessageArguments() {
        BizException exception = new BizException("AUTH-00001", "auth.exception.invalid-captcha", "验证码错误", "login");

        assertEquals("AUTH-00001", exception.getCode());
        assertEquals("auth.exception.invalid-captcha", exception.getMessageKey());
        assertEquals("验证码错误", exception.getDefaultMessage());
        assertEquals("验证码错误", exception.getMessage());
        assertArrayEquals(new Object[] {"login"}, exception.getMessageArgs());
    }

    @Test
    public void shouldKeepCause() {
        RuntimeException cause = new RuntimeException("boom");
        BizException exception = new BizException("COMMON-00006", "common.exception.system-error", "系统异常", cause);

        assertSame(cause, exception.getCause());
    }

    @Test
    public void shouldProtectMessageArgsFromExternalMutation() {
        Object[] args = new Object[] {"before"};
        BizException exception = new BizException("AUTH-00003", "auth.exception.invalid-password", "密码错误", args);

        args[0] = "after";
        Object[] copiedArgs = exception.getMessageArgs();
        copiedArgs[0] = "changed";

        assertArrayEquals(new Object[] {"before"}, exception.getMessageArgs());
    }
}
