package com.thundax.kuzhambu.system.interfaces.admin.core.mq;

import com.thundax.kuzhambu.system.interfaces.admin.core.service.SysLogMessageService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "${kuzhambu.log.sys.topic:" + SysLogMessageService.TOPIC_SAVE_LOG + "}",
        consumerGroup = "${kuzhambu.log.sys.consumer-group:kuzhambu-admin-starter-sys-log-consumer}",
        selectorExpression = "*")
public class RocketMqSysLogConsumer implements RocketMQListener<String> {

    private final SysLogMessageService sysLogMessageService;

    public RocketMqSysLogConsumer(SysLogMessageService sysLogMessageService) {
        this.sysLogMessageService = sysLogMessageService;
    }

    @Override
    public void onMessage(String payload) {
        sysLogMessageService.consumeLog(payload);
    }
}
