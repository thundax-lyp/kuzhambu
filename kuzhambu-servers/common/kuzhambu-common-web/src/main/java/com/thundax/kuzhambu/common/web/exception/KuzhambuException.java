package com.thundax.kuzhambu.common.web.exception;

public class KuzhambuException extends RuntimeException {

    private final WebErrorCode errorCode;
    private final String code;
    private final String messageKey;
    private final String defaultMessage;
    private final Object[] messageArgs;

    public KuzhambuException(WebErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
        this.messageKey = errorCode.getMessageKey();
        this.defaultMessage = errorCode.getMessage();
        this.messageArgs = new Object[0];
    }

    public KuzhambuException(WebErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
        this.messageKey = errorCode.getMessageKey();
        this.defaultMessage = message;
        this.messageArgs = new Object[0];
    }

    public KuzhambuException(WebErrorCode errorCode, String code, String message) {
        super(message);
        this.errorCode = errorCode;
        this.code = code;
        this.messageKey = errorCode.getMessageKey();
        this.defaultMessage = message;
        this.messageArgs = new Object[0];
    }

    public KuzhambuException(
            WebErrorCode errorCode, String code, String messageKey, String defaultMessage, Object... messageArgs) {
        super(defaultMessage);
        this.errorCode = errorCode;
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public WebErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
