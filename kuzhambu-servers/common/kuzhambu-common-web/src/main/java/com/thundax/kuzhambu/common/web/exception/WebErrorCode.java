package com.thundax.kuzhambu.common.web.exception;

public enum WebErrorCode {
    BAD_REQUEST("COMMON-00001", 400, "common.exception.bad-request", "请求参数错误"),
    UNAUTHORIZED("COMMON-00002", 401, "common.exception.unauthorized", "未认证"),
    FORBIDDEN("COMMON-00003", 403, "common.exception.forbidden", "无访问权限"),
    NOT_FOUND("COMMON-00004", 404, "common.exception.not-found", "资源不存在"),
    CONFLICT("COMMON-00005", 409, "common.exception.conflict", "资源状态冲突"),
    SYSTEM_ERROR("COMMON-00006", 500, "common.exception.system-error", "系统异常");

    private final String code;
    private final int httpStatus;
    private final String messageKey;
    private final String message;

    WebErrorCode(String code, int httpStatus, String messageKey, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.messageKey = messageKey;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getMessage() {
        return message;
    }
}
