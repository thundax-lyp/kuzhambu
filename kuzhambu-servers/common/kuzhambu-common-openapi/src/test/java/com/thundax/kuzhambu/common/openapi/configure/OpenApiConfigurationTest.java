package com.thundax.kuzhambu.common.openapi.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
            assertEquals(7, context.getBeansOfType(GroupedOpenApi.class).size());
            assertEquals(
                    "Kuzhambu API", context.getBean(OpenAPI.class).getInfo().getTitle());
        });
    }

    @Test
    public void shouldCreateModuleGroupedOpenApiBeansWithMethodFilters() {
        contextRunner.run(context -> {
            Map<String, GroupedOpenApi> beans = context.getBeansOfType(GroupedOpenApi.class);
            Set<String> groups =
                    beans.values().stream().map(GroupedOpenApi::getGroup).collect(Collectors.toSet());

            assertEquals(Set.of("system", "storage", "classics", "ai", "knowledge", "discovery", "operations"), groups);
            assertTrue(beans.values().stream()
                    .allMatch(groupedOpenApi ->
                            groupedOpenApi.getOpenApiMethodFilters().size() == 1));
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
