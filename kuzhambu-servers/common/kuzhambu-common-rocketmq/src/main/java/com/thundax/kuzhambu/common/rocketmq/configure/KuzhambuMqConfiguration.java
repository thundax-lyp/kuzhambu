package com.thundax.kuzhambu.common.rocketmq.configure;

import com.thundax.kuzhambu.common.rocketmq.KuzhambuMqSender;
import com.thundax.kuzhambu.common.rocketmq.support.RocketMqKuzhambuMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
@AutoConfigureAfter(name = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
public class KuzhambuMqConfiguration {

    @Bean
    public KuzhambuMqConfigurationValidator kuzhambuMqConfigurationValidator(
            ObjectProvider<KuzhambuMqSender> senderProvider, Environment environment) {
        return new KuzhambuMqConfigurationValidator(senderProvider, environment);
    }

    @Bean
    @ConditionalOnClass(RocketMQTemplate.class)
    @ConditionalOnBean(RocketMQTemplate.class)
    @ConditionalOnMissingBean(KuzhambuMqSender.class)
    public KuzhambuMqSender rocketMqSender(RocketMQTemplate rocketMQTemplate) {
        return new RocketMqKuzhambuMqSender(rocketMQTemplate);
    }

    public static class KuzhambuMqConfigurationValidator implements InitializingBean {

        private final ObjectProvider<KuzhambuMqSender> senderProvider;
        private final Environment environment;

        public KuzhambuMqConfigurationValidator(
                ObjectProvider<KuzhambuMqSender> senderProvider, Environment environment) {
            this.senderProvider = senderProvider;
            this.environment = environment;
        }

        @Override
        public void afterPropertiesSet() {
            requireText(environment.getProperty("rocketmq.name-server"), "rocketmq.name-server");
            if (senderProvider.getIfAvailable() == null) {
                throw new IllegalStateException(
                        "RocketMQ is enabled but no KuzhambuMqSender was created. Check broker client configuration.");
            }
        }

        private void requireText(String value, String propertyName) {
            if (!StringUtils.hasText(value)) {
                throw new IllegalStateException("Missing RocketMQ configuration. Configure " + propertyName + ".");
            }
        }
    }
}
