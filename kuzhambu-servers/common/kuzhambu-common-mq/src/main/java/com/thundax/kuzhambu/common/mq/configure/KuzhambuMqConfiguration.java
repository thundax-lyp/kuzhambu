package com.thundax.kuzhambu.common.mq.configure;

import com.thundax.kuzhambu.common.mq.KuzhambuMqSender;
import com.thundax.kuzhambu.common.mq.support.NoOpKuzhambuMqSender;
import com.thundax.kuzhambu.common.mq.support.RabbitKuzhambuMqSender;
import com.thundax.kuzhambu.common.mq.support.RocketMqKuzhambuMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
@AutoConfigureAfter(
        name = {
            "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
            "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration"
        })
@EnableConfigurationProperties(KuzhambuMqProperties.class)
public class KuzhambuMqConfiguration {

    @Bean
    public KuzhambuMqConfigurationValidator kuzhambuMqConfigurationValidator(
            KuzhambuMqProperties properties, ObjectProvider<KuzhambuMqSender> senderProvider, Environment environment) {
        return new KuzhambuMqConfigurationValidator(properties, senderProvider, environment);
    }

    @Bean
    @ConditionalOnProperty(prefix = "kuzhambu.mq", name = "enabled", havingValue = "false")
    @ConditionalOnMissingBean(KuzhambuMqSender.class)
    public KuzhambuMqSender disabledKuzhambuMqSender() {
        return new NoOpKuzhambuMqSender();
    }

    @Bean
    @ConditionalOnClass(RabbitTemplate.class)
    @ConditionalOnBean(RabbitTemplate.class)
    @ConditionalOnProperty(prefix = "kuzhambu.mq", name = "type", havingValue = "RABBITMQ", matchIfMissing = true)
    @ConditionalOnMissingBean(KuzhambuMqSender.class)
    public KuzhambuMqSender rabbitMqSender(RabbitTemplate rabbitTemplate) {
        return new RabbitKuzhambuMqSender(rabbitTemplate);
    }

    @Bean
    @ConditionalOnClass(RocketMQTemplate.class)
    @ConditionalOnBean(RocketMQTemplate.class)
    @ConditionalOnProperty(prefix = "kuzhambu.mq", name = "type", havingValue = "ROCKETMQ")
    @ConditionalOnMissingBean(KuzhambuMqSender.class)
    public KuzhambuMqSender rocketMqSender(RocketMQTemplate rocketMQTemplate) {
        return new RocketMqKuzhambuMqSender(rocketMQTemplate);
    }

    public static class KuzhambuMqConfigurationValidator implements InitializingBean {

        private final KuzhambuMqProperties properties;
        private final ObjectProvider<KuzhambuMqSender> senderProvider;
        private final Environment environment;

        public KuzhambuMqConfigurationValidator(
                KuzhambuMqProperties properties,
                ObjectProvider<KuzhambuMqSender> senderProvider,
                Environment environment) {
            this.properties = properties;
            this.senderProvider = senderProvider;
            this.environment = environment;
        }

        @Override
        public void afterPropertiesSet() {
            if (!properties.isEnabled()) {
                return;
            }
            if (properties.getType() == null) {
                throw new IllegalStateException("Missing MQ configuration. Configure kuzhambu.mq.type.");
            }
            switch (properties.getType()) {
                case RABBITMQ:
                    validateSender("RabbitMQ");
                    return;
                case ROCKETMQ:
                    requireText(environment.getProperty("rocketmq.name-server"), "rocketmq.name-server");
                    validateSender("RocketMQ");
                    return;
                default:
                    throw new IllegalStateException("Unsupported MQ type: " + properties.getType());
            }
        }

        private void validateSender(String name) {
            if (senderProvider.getIfAvailable() == null) {
                throw new IllegalStateException(
                        name + " is enabled but no KuzhambuMqSender was created. Check broker client configuration.");
            }
        }

        private void requireText(String value, String propertyName) {
            if (!StringUtils.hasText(value)) {
                throw new IllegalStateException("Missing RocketMQ configuration. Configure " + propertyName + ".");
            }
        }
    }
}
