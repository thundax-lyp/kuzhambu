package com.thundax.kuzhambu.common.mq.support;

import com.thundax.kuzhambu.common.mq.KuzhambuMqMessage;
import com.thundax.kuzhambu.common.mq.KuzhambuMqSender;

public class NoOpKuzhambuMqSender implements KuzhambuMqSender {

    @Override
    public void send(KuzhambuMqMessage message) {
        // Default sender intentionally discards messages when no broker adapter is configured.
    }
}
