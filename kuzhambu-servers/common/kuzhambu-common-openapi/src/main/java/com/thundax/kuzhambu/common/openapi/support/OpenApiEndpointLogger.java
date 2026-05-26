package com.thundax.kuzhambu.common.openapi.support;

import com.thundax.kuzhambu.common.openapi.configure.OpenApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

public class OpenApiEndpointLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiEndpointLogger.class);
    private static final String LOCALHOST = "127.0.0.1";
    private static final String SWAGGER_UI_PATH = "/swagger-ui/index.html";

    private final OpenApiProperties properties;

    public OpenApiEndpointLogger(OpenApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        Environment environment = event.getApplicationContext().getEnvironment();
        String port = environment.getProperty("local.server.port", environment.getProperty("server.port", "8080"));
        String contextPath = environment.getProperty("server.servlet.context-path", "");

        LOGGER.info(
                "OpenAPI UI: http://{}:{}{}{}", LOCALHOST, port, normalizeContextPath(contextPath), SWAGGER_UI_PATH);
    }

    private String normalizeContextPath(String contextPath) {
        if (contextPath == null || contextPath.trim().isEmpty() || "/".equals(contextPath.trim())) {
            return "";
        }
        String normalizedContextPath = contextPath.trim();
        return normalizedContextPath.startsWith("/") ? normalizedContextPath : "/" + normalizedContextPath;
    }
}
