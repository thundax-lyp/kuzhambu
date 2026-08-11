package com.thundax.kuzhambu.common.web.exception;

public class ApiException extends KuzhambuException {

    public ApiException(String message) {
        super(WebErrorCode.SYSTEM_ERROR, message);
    }

    public ApiException(String code, String messageKey, String defaultMessage, Throwable cause) {
        super(WebErrorCode.SYSTEM_ERROR, code, messageKey, defaultMessage);
        initCause(cause);
    }

    public ApiException(WebErrorCode errorCode) {
        super(errorCode);
    }

    public ApiException(WebErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ApiException(WebErrorCode errorCode, String code, String message) {
        super(errorCode, code, message);
    }

    public ApiException(
            WebErrorCode errorCode, String code, String messageKey, String defaultMessage, Object... messageArgs) {
        super(errorCode, code, messageKey, defaultMessage, messageArgs);
    }
}
