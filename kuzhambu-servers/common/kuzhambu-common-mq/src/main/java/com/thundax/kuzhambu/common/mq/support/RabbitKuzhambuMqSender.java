package com.thundax.kuzhambu.common.mq.support;

import com.thundax.kuzhambu.common.mq.KuzhambuMqMessage;
import com.thundax.kuzhambu.common.mq.KuzhambuMqSender;
import java.util.Map;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitKuzhambuMqSender implements KuzhambuMqSender {

    private final RabbitTemplate rabbitTemplate;

    public RabbitKuzhambuMqSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(KuzhambuMqMessage message) {
        MessagePostProcessor postProcessor = mqMessage -> {
            for (Map.Entry<String, String> entry : message.getHeaders().entrySet()) {
                mqMessage.getMessageProperties().setHeader(entry.getKey(), entry.getValue());
            }
            return mqMessage;
        };
        if (message.getExchange() == null || message.getExchange().trim().isEmpty()) {
            rabbitTemplate.convertAndSend(message.getRoutingKey(), message.getPayload(), postProcessor);
            return;
        }
        rabbitTemplate.convertAndSend(
                message.getExchange(), message.getRoutingKey(), message.getPayload(), postProcessor);
    }
}
