package com.thundax.kuzhambu.common.mq;

public interface KuzhambuMqSender {

    void send(KuzhambuMqMessage message);
}
