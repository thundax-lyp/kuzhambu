package com.thundax.kuzhambu.common.rocketmq.configure;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.thundax.kuzhambu.common.rocketmq.KuzhambuMqSender;
import com.thundax.kuzhambu.common.rocketmq.support.RocketMqKuzhambuMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class KuzhambuMqConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(KuzhambuMqConfiguration.class));

    @Test
    public void shouldRejectRocketMqWhenNameServerMissing() {
        contextRunner
                .withUserConfiguration(RocketMqTemplateConfiguration.class)
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldCreateRocketSender() {
        contextRunner
                .withUserConfiguration(RocketMqTemplateConfiguration.class)
                .withPropertyValues("rocketmq.name-server=127.0.0.1:9876")
                .run(context -> {
                    KuzhambuMqSender sender = context.getBean(KuzhambuMqSender.class);
                    assertTrue(sender instanceof RocketMqKuzhambuMqSender);
                });
    }

    @Test
    public void shouldKeepCustomSender() {
        contextRunner
                .withUserConfiguration(CustomSenderConfiguration.class)
                .withPropertyValues("rocketmq.name-server=127.0.0.1:9876")
                .run(context -> {
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
