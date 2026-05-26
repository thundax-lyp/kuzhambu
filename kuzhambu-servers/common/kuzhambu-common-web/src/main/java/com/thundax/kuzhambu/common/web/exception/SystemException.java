package com.thundax.kuzhambu.common.web.exception;

public class SystemException extends KuzhambuException {

    public SystemException() {
        super(WebErrorCode.SYSTEM_ERROR);
    }

    public SystemException(String message) {
        super(WebErrorCode.SYSTEM_ERROR, message);
    }
}
