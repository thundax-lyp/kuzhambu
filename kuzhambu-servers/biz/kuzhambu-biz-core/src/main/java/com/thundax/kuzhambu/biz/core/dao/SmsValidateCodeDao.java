package com.thundax.kuzhambu.biz.core.dao;

public interface SmsValidateCodeDao {

    boolean canSend(String mobile);

    void markSent(String mobile, int expiredSeconds);
}
