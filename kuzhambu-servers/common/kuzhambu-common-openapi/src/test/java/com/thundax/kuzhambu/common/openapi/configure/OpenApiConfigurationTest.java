package com.thundax.kuzhambu.common.openapi.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class OpenApiConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(OpenApiConfiguration.class));

    @Test
    public void shouldCreateOpenApiBeansWithDefaultProperties() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(OpenApiProperties.class));
            assertNotNull(context.getBean(OpenAPI.class));
            assertNotNull(context.getBean(GroupedOpenApi.class));
            assertEquals(
                    "Kuzhambu API", context.getBean(OpenAPI.class).getInfo().getTitle());
        });
    }

    @Test
    public void shouldBindEnabledProperty() {
        contextRunner.withPropertyValues("kuzhambu.openapi.enabled=false").run(context -> {
            assertEquals(0, context.getBeansOfType(OpenAPI.class).size());
            assertEquals(0, context.getBeansOfType(GroupedOpenApi.class).size());
        });
    }
}
