package com.thundax.kuzhambu.system.domain.core.repository;

public interface SmsValidateCodeRepository {

    boolean canSend(String mobile);

    void markSent(String mobile, int expiredSeconds);
}
