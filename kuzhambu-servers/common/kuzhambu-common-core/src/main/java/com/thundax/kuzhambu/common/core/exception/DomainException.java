package com.thundax.kuzhambu.common.core.exception;

public class DomainException extends BizException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String code, String messageKey, String defaultMessage, Object... messageArgs) {
        super(code, messageKey, defaultMessage, messageArgs);
    }

    public DomainException(String code, String messageKey, String defaultMessage, Throwable cause) {
        super(code, messageKey, defaultMessage, cause);
    }

    public DomainException(
            String code, String messageKey, String defaultMessage, Throwable cause, Object... messageArgs) {
        super(code, messageKey, defaultMessage, cause, messageArgs);
    }
}
