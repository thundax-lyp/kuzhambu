package com.thundax.kuzhambu.system.application.core.dao;

public interface SmsValidateCodeDao {

    boolean canSend(String mobile);

    void markSent(String mobile, int expiredSeconds);
}
