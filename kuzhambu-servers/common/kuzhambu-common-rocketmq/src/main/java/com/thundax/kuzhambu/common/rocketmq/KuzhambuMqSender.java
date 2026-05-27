package com.thundax.kuzhambu.common.rocketmq;

public interface KuzhambuMqSender {

    void send(KuzhambuMqMessage message);
}
