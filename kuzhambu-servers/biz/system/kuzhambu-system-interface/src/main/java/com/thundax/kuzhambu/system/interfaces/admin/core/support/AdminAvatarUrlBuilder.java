package com.thundax.kuzhambu.system.interfaces.admin.core.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AdminAvatarUrlBuilder {

    private static final String AVATAR_PATH = "/api/sys/user/avatar";

    private final String configuredContextPath;

    public AdminAvatarUrlBuilder(@Value("${server.servlet.context-path:}") String configuredContextPath) {
        this.configuredContextPath = configuredContextPath;
    }

    public String build(String userId) {
        return UriComponentsBuilder.fromPath(currentContextPath())
                .path(AVATAR_PATH)
                .queryParam("id", userId)
                .build()
                .toUriString();
    }

    private String currentContextPath() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            String requestContextPath = servletRequestAttributes.getRequest().getContextPath();
            if (StringUtils.isNotBlank(requestContextPath)) {
                return requestContextPath;
            }
        }
        return StringUtils.defaultString(configuredContextPath);
    }
}
