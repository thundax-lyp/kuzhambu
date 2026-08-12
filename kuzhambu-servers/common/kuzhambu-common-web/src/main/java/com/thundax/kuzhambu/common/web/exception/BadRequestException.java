package com.thundax.kuzhambu.common.web.exception;

public class BadRequestException extends ApiException {

    public BadRequestException() {
        super(WebErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(WebErrorCode.BAD_REQUEST, message);
    }
}
