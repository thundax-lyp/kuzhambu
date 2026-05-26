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
    @ConditionalOnMissingBean
    public GroupedOpenApi kuzhambuGroupedOpenApi(OpenApiProperties properties) {
        return GroupedOpenApi.builder()
                .group("kuzhambu")
                .packagesToScan(basePackages(properties))
                .pathsToMatch("/**")
                .build();
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

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
