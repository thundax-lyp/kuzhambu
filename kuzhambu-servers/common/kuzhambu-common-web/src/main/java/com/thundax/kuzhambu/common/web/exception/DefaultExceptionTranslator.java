package com.thundax.kuzhambu.common.web.exception;

import org.springframework.core.Ordered;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class DefaultExceptionTranslator implements ExceptionTranslator, Ordered {

    @Override
    public KuzhambuException translate(Exception exception) {
        if (exception instanceof AccessDeniedException) {
            return new KuzhambuException(WebErrorCode.FORBIDDEN);
        }
        if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validException = (MethodArgumentNotValidException) exception;
            return new BadRequestException(
                    firstFieldErrorMessage(validException.getBindingResult().getFieldError()));
        }
        if (exception instanceof BindException) {
            BindException bindException = (BindException) exception;
            return new BadRequestException(firstFieldErrorMessage(bindException.getFieldError()));
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private String firstFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return WebErrorCode.BAD_REQUEST.getMessage();
        }
        return fieldError.getDefaultMessage();
    }
}
