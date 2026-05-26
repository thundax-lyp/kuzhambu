package com.thundax.kuzhambu.biz.auth.exception;

import com.thundax.kuzhambu.common.core.exception.BizException;

public class InvalidCaptchaException extends BizException {

    public InvalidCaptchaException() {
        super("AUTH-00001", "auth.exception.invalid-captcha", "验证码错误");
    }
}
