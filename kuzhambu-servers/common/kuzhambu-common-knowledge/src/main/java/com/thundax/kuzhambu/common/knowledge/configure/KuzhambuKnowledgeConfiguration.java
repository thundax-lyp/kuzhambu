package com.thundax.kuzhambu.common.knowledge.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.support.FastGptKnowledgeBaseClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(KuzhambuKnowledgeProperties.class)
@ConditionalOnProperty(prefix = "kuzhambu.knowledge", name = "enabled", havingValue = "true")
public class KuzhambuKnowledgeConfiguration {

    @Bean
    public KuzhambuKnowledgeConfigurationValidator kuzhambuKnowledgeConfigurationValidator(
            KuzhambuKnowledgeProperties properties) {
        return new KuzhambuKnowledgeConfigurationValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean(KnowledgeBaseClient.class)
    @ConditionalOnProperty(
            prefix = "kuzhambu.knowledge",
            name = "provider",
            havingValue = "fastgpt",
            matchIfMissing = true)
    public KnowledgeBaseClient fastGptKnowledgeBaseClient(
            KuzhambuKnowledgeProperties properties, ObjectProvider<ObjectMapper> objectMappers) {
        KuzhambuKnowledgeProperties.FastGpt fastGpt = properties.getFastgpt();
        return new FastGptKnowledgeBaseClient(
                new RestTemplateBuilder()
                        .rootUri(fastGpt.getBaseUrl())
                        .connectTimeout(fastGpt.getTimeout())
                        .readTimeout(fastGpt.getTimeout())
                        .build(),
                objectMappers.getIfAvailable(ObjectMapper::new),
                fastGpt);
    }

    public static class KuzhambuKnowledgeConfigurationValidator implements InitializingBean {

        private final KuzhambuKnowledgeProperties properties;

        public KuzhambuKnowledgeConfigurationValidator(KuzhambuKnowledgeProperties properties) {
            this.properties = properties;
        }

        @Override
        public void afterPropertiesSet() {
            if (!StringUtils.hasText(properties.getProvider())) {
                throw new IllegalStateException(
                        "Missing knowledge provider configuration. Configure kuzhambu.knowledge.provider.");
            }
            if (!"fastgpt".equalsIgnoreCase(properties.getProvider())) {
                throw new IllegalStateException("Unsupported knowledge provider: " + properties.getProvider()
                        + ". Kuzhambu currently supports fastgpt.");
            }
            validateFastGpt();
        }

        private void validateFastGpt() {
            KuzhambuKnowledgeProperties.FastGpt fastGpt = properties.getFastgpt();
            if (fastGpt == null) {
                throw new IllegalStateException(
                        "Missing FastGPT knowledge configuration. Configure kuzhambu.knowledge.fastgpt.");
            }
            requireText(fastGpt.getBaseUrl(), "kuzhambu.knowledge.fastgpt.base-url");
            requireText(fastGpt.getApiKey(), "kuzhambu.knowledge.fastgpt.api-key");
            if (fastGpt.getTimeout() == null
                    || fastGpt.getTimeout().isNegative()
                    || fastGpt.getTimeout().isZero()) {
                throw new IllegalStateException(
                        "Invalid FastGPT knowledge configuration. Configure kuzhambu.knowledge.fastgpt.timeout.");
            }
        }

        private void requireText(String value, String propertyName) {
            if (!StringUtils.hasText(value)) {
                throw new IllegalStateException(
                        "Missing FastGPT knowledge configuration. Configure " + propertyName + ".");
            }
        }
    }
}
