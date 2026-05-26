package com.thundax.kuzhambu.common.mq.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.thundax.kuzhambu.common.mq.KuzhambuMqSender;
import com.thundax.kuzhambu.common.mq.KuzhambuMqType;
import com.thundax.kuzhambu.common.mq.support.NoOpKuzhambuMqSender;
import com.thundax.kuzhambu.common.mq.support.RabbitKuzhambuMqSender;
import com.thundax.kuzhambu.common.mq.support.RocketMqKuzhambuMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class KuzhambuMqConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(KuzhambuMqConfiguration.class));

    @Test
    public void shouldRejectEnabledMqWhenNoBrokerClientConfigured() {
        contextRunner.run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldCreatePropertiesWithoutSenderWhenDisabledAndNoBrokerClientConfigured() {
        contextRunner.withPropertyValues("kuzhambu.mq.enabled=false").run(context -> {
            assertNotNull(context.getBean(KuzhambuMqProperties.class));
            assertTrue(context.getBean(KuzhambuMqSender.class) instanceof NoOpKuzhambuMqSender);
        });
    }

    @Test
    public void shouldBindProperties() {
        contextRunner
                .withPropertyValues("kuzhambu.mq.enabled=false", "kuzhambu.mq.type=ROCKETMQ")
                .run(context -> {
                    KuzhambuMqProperties properties = context.getBean(KuzhambuMqProperties.class);
                    assertFalse(properties.isEnabled());
                    assertEquals(KuzhambuMqType.ROCKETMQ, properties.getType());
                });
    }

    @Test
    public void shouldCreateNoOpSenderWhenDisabled() {
        contextRunner.withPropertyValues("kuzhambu.mq.enabled=false").run(context -> {
            KuzhambuMqSender sender = context.getBean(KuzhambuMqSender.class);
            assertTrue(sender instanceof NoOpKuzhambuMqSender);
        });
    }

    @Test
    public void shouldCreateRabbitSenderWhenRabbitTypeConfigured() {
        contextRunner
                .withUserConfiguration(RabbitTemplateConfiguration.class)
                .withPropertyValues("kuzhambu.mq.type=RABBITMQ")
                .run(context -> {
                    KuzhambuMqSender sender = context.getBean(KuzhambuMqSender.class);
                    assertTrue(sender instanceof RabbitKuzhambuMqSender);
                });
    }

    @Test
    public void shouldCreateRocketSenderWhenRocketTypeConfigured() {
        contextRunner
                .withUserConfiguration(RocketMqTemplateConfiguration.class)
                .withPropertyValues("kuzhambu.mq.type=ROCKETMQ", "rocketmq.name-server=127.0.0.1:9876")
                .run(context -> {
                    KuzhambuMqSender sender = context.getBean(KuzhambuMqSender.class);
                    assertTrue(sender instanceof RocketMqKuzhambuMqSender);
                });
    }

    @Test
    public void shouldRejectRocketMqWhenNameServerMissing() {
        contextRunner
                .withUserConfiguration(RocketMqTemplateConfiguration.class)
                .withPropertyValues("kuzhambu.mq.type=ROCKETMQ")
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldKeepCustomSender() {
        contextRunner.withUserConfiguration(CustomSenderConfiguration.class).run(context -> {
            KuzhambuMqSender sender = context.getBean(KuzhambuMqSender.class);
            assertSame(CustomSenderConfiguration.SENDER, sender);
        });
    }

    @Configuration
    static class CustomSenderConfiguration {

        private static final KuzhambuMqSender SENDER = message -> {};

        @Bean
        public KuzhambuMqSender kuzhambuMqSender() {
            return SENDER;
        }
    }

    @Configuration
    static class RabbitTemplateConfiguration {

        @Bean
        public RabbitTemplate rabbitTemplate() {
            return mock(RabbitTemplate.class);
        }
    }

    @Configuration
    static class RocketMqTemplateConfiguration {

        @Bean
        public RocketMQTemplate rocketMQTemplate() {
            return mock(RocketMQTemplate.class);
        }
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable cause = throwable;
        while (cause != null) {
            if (causeType.isInstance(cause)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
