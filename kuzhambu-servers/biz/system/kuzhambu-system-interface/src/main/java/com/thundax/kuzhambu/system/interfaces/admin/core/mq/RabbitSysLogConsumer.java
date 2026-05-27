package com.thundax.kuzhambu.system.interfaces.admin.core.mq;

import com.thundax.kuzhambu.system.interfaces.admin.core.service.SysLogMessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "kuzhambu.mq", name = "type", havingValue = "RABBITMQ", matchIfMissing = true)
public class RabbitSysLogConsumer {

    private final SysLogMessageService sysLogMessageService;

    public RabbitSysLogConsumer(SysLogMessageService sysLogMessageService) {
        this.sysLogMessageService = sysLogMessageService;
    }

    @RabbitListener(
            queues = "${kuzhambu.log.sys.queue:" + SysLogMessageService.QUEUE_SAVE_LOG + "}",
            concurrency = "${kuzhambu.log.sys.rabbit.concurrency:2}")
    public void onMessage(@Payload String payload) {
        sysLogMessageService.consumeLog(payload);
    }
}
