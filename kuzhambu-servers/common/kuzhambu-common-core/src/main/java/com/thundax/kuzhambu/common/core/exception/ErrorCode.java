package com.thundax.kuzhambu.common.core.exception;

public enum ErrorCode {
    SORT_EMPTY_INPUT("SORT-00001", "biz.sort-empty-input", "排序输入不能为空"),
    SORT_DUPLICATE_ID("SORT-00002", "biz.sort-duplicate-id", "排序实体存在重复 ID"),
    SORT_MISSING_ID("SORT-00003", "biz.sort-missing-id", "排序实体集合与查询范围不一致"),
    SORT_CONCURRENT_MODIFICATION("SORT-00004", "biz.sort-concurrent-modification", "排序存在并发修改，请重试"),
    SORT_DB_FAILURE("SORT-00005", "biz.sort-db-failure", "排序数据库异常");

    private final String code;
    private final String messageKey;
    private final String message;

    ErrorCode(String code, String messageKey, String message) {
        this.code = code;
        this.messageKey = messageKey;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getMessage() {
        return message;
    }
}
