package com.thundax.kuzhambu.common.knowledge.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseListRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class KuzhambuKnowledgeConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KuzhambuKnowledgeConfiguration.class));

    @Test
    public void shouldNotCreateKnowledgeClientWhenDisabled() {
        contextRunner.run(context -> {
            assertFalse(context.containsBean("fastGptKnowledgeBaseClient"));
            KnowledgeBaseClient client = context.getBean(KnowledgeBaseClient.class);
            assertFalse(client.health().available());
            assertThrows(
                    IllegalStateException.class,
                    () -> client.listKnowledgeBases(new KnowledgeBaseListRequest(1, 20, "Discovery")));
        });
    }

    @Test
    public void shouldCreateFastGptKnowledgeClientWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "kuzhambu.knowledge.enabled=true",
                        "kuzhambu.knowledge.fastgpt.api-key=fastgpt-test",
                        "kuzhambu.knowledge.fastgpt.app-id=app-1",
                        "kuzhambu.knowledge.fastgpt.knowledge-base-id=6a4f51e5ef72393d430a8e31")
                .run(context -> assertTrue(context.getBean(KnowledgeBaseClient.class) != null));
    }

    @Test
    public void shouldBindFastGptAppIdFromAppidPropertyName() {
        contextRunner
                .withPropertyValues(
                        "kuzhambu.knowledge.enabled=true",
                        "kuzhambu.knowledge.fastgpt.api-key=fastgpt-test",
                        "kuzhambu.knowledge.fastgpt.appid=app-1",
                        "kuzhambu.knowledge.fastgpt.knowledge-base-id=6a4f51e5ef72393d430a8e31")
                .run(context -> assertEquals(
                        "app-1",
                        context.getBean(KuzhambuKnowledgeProperties.class)
                                .getFastgpt()
                                .getAppId()));
    }

    @Test
    public void shouldRejectInvalidFastGptKnowledgeBaseIdWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "kuzhambu.knowledge.enabled=true",
                        "kuzhambu.knowledge.fastgpt.api-key=fastgpt-test",
                        "kuzhambu.knowledge.fastgpt.app-id=app-1",
                        "kuzhambu.knowledge.fastgpt.knowledge-base-id=kb-kuzhambu-qa")
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
    }

    @Test
    public void shouldRejectMissingFastGptApiKeyWhenEnabled() {
        contextRunner
                .withPropertyValues("kuzhambu.knowledge.enabled=true")
                .run(context -> assertTrue(hasCause(context.getStartupFailure(), IllegalStateException.class)));
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
