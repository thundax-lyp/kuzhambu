package com.thundax.kuzhambu.common.openapi.configure;

import com.thundax.kuzhambu.common.openapi.support.OpenApiEndpointLogger;
import com.thundax.kuzhambu.common.openapi.support.OpenApiOperationOrdering;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(GroupedOpenApi.class)
@EnableConfigurationProperties(OpenApiProperties.class)
@ConditionalOnProperty(prefix = "kuzhambu.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfiguration {

    private static final String PACKAGE_SEPARATOR = ",";

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openAPI(OpenApiProperties properties) {
        return new OpenAPI().info(info(properties));
    }

    @Bean
    @ConditionalOnMissingBean(name = "systemGroupedOpenApi")
    public GroupedOpenApi systemGroupedOpenApi(OpenApiProperties properties) {
        return moduleGroupedOpenApi(properties, "system", "系统模块");
    }

    @Bean
    @ConditionalOnMissingBean(name = "storageGroupedOpenApi")
    public GroupedOpenApi storageGroupedOpenApi(OpenApiProperties properties) {
        return moduleGroupedOpenApi(properties, "storage", "存储模块");
    }

    @Bean
    @ConditionalOnMissingBean(name = "classicsGroupedOpenApi")
    public GroupedOpenApi classicsGroupedOpenApi(OpenApiProperties properties) {
        return moduleGroupedOpenApi(properties, "classics", "古籍模块");
    }

    @Bean
    @ConditionalOnMissingBean(name = "aiGroupedOpenApi")
    public GroupedOpenApi aiGroupedOpenApi(OpenApiProperties properties) {
        return moduleGroupedOpenApi(properties, "ai", "智能模块");
    }

    @Bean
    @ConditionalOnMissingBean(name = "knowledgeGroupedOpenApi")
    public GroupedOpenApi knowledgeGroupedOpenApi(OpenApiProperties properties) {
        return moduleGroupedOpenApi(properties, "knowledge", "知识模块");
    }

    @Bean
    @ConditionalOnMissingBean(name = "discoveryGroupedOpenApi")
    public GroupedOpenApi discoveryGroupedOpenApi(OpenApiProperties properties) {
        return moduleGroupedOpenApi(properties, "discovery", "发现模块");
    }

    @Bean
    @ConditionalOnMissingBean(name = "operationsGroupedOpenApi")
    public GroupedOpenApi operationsGroupedOpenApi(OpenApiProperties properties) {
        return moduleGroupedOpenApi(properties, "operations", "运营模块");
    }

    @Bean
    @ConditionalOnMissingBean(name = "openApiOperationOrderingCustomizer")
    public OpenApiCustomizer openApiOperationOrderingCustomizer() {
        return OpenApiOperationOrdering::sort;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenApiEndpointLogger openApiEndpointLogger(OpenApiProperties properties) {
        return new OpenApiEndpointLogger(properties);
    }

    private Info info(OpenApiProperties properties) {
        Info info = new Info()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .termsOfService(properties.getTermsOfServiceUrl())
                .version(properties.getVersion())
                .contact(new Contact()
                        .name(properties.getContactName())
                        .url(properties.getContactUrl())
                        .email(properties.getContactEmail()));
        if (hasText(properties.getLicense()) || hasText(properties.getLicenseUrl())) {
            info.license(new License().name(properties.getLicense()).url(properties.getLicenseUrl()));
        }
        return info;
    }

    private String[] basePackages(OpenApiProperties properties) {
        String basePackage = properties.getBasePackage();
        if (!hasText(basePackage)) {
            return new String[] {"com.thundax.kuzhambu"};
        }
        return java.util.Arrays.stream(basePackage.split(PACKAGE_SEPARATOR))
                .map(String::trim)
                .filter(this::hasText)
                .toArray(String[]::new);
    }

    private GroupedOpenApi moduleGroupedOpenApi(OpenApiProperties properties, String module, String displayName) {
        return GroupedOpenApi.builder()
                .group(module)
                .displayName(displayName)
                .packagesToScan(basePackages(properties))
                .pathsToMatch("/**")
                .addOpenApiMethodFilter(
                        method -> isModuleMethod(method.getDeclaringClass().getPackageName(), module))
                .build();
    }

    private boolean isModuleMethod(String packageName, String module) {
        return packageName.startsWith("com.thundax.kuzhambu." + module + ".");
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
