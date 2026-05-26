package com.thundax.kuzhambu.common.web.exception;

public interface ExceptionTranslator {

    KuzhambuException translate(Exception exception);
}
