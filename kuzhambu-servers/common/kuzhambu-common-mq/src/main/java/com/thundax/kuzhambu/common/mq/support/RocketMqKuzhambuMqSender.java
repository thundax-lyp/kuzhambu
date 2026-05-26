package com.thundax.kuzhambu.common.mq.support;

import com.thundax.kuzhambu.common.mq.KuzhambuMqMessage;
import com.thundax.kuzhambu.common.mq.KuzhambuMqSender;
import java.util.Map;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.support.MessageBuilder;

public class RocketMqKuzhambuMqSender implements KuzhambuMqSender {

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMqKuzhambuMqSender(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void send(KuzhambuMqMessage message) {
        MessageBuilder<Object> builder = MessageBuilder.withPayload(message.getPayload());
        if (message.getKey() != null) {
            builder.setHeader(RocketMQHeaders.KEYS, message.getKey());
        }
        if (message.getTag() != null) {
            builder.setHeader(RocketMQHeaders.TAGS, message.getTag());
        }
        for (Map.Entry<String, String> entry : message.getHeaders().entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        rocketMQTemplate.syncSend(buildDestination(message), builder.build());
    }

    private String buildDestination(KuzhambuMqMessage message) {
        if (message.getTag() == null || message.getTag().trim().isEmpty()) {
            return message.getTopic();
        }
        return message.getTopic() + ":" + message.getTag();
    }
}
