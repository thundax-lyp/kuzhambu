package com.thundax.kuzhambu.common.web.restore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kuzhambu.web.restore-write-block")
public class RestoreWriteBlockProperties {

    private boolean enabled = true;

    private List<String> allowedPaths = new ArrayList<>(List.of(
            "/api/operations/restore/execute",
            "/api/operations/restore/page",
            "/api/operations/restore/detail",
            "/api/operations/backup/page",
            "/api/operations/backup/detail",
            "/api/auth/session/pre-auth-session",
            "/api/auth/session/pre-auth-session/refresh",
            "/api/auth/session/login",
            "/api/auth/session/login/sms",
            "/api/auth/session/login/wecom",
            "/api/auth/session/login/github",
            "/api/auth/session/logout",
            "/api/auth/session/token/verify",
            "/api/auth/session/token/refresh",
            "/api/auth/captcha",
            "/api/auth/captcha/refresh",
            "/actuator/health"));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAllowedPaths() {
        return Collections.unmodifiableList(allowedPaths);
    }

    public void setAllowedPaths(List<String> allowedPaths) {
        if (allowedPaths == null) {
            this.allowedPaths = new ArrayList<>();
            return;
        }
        this.allowedPaths = allowedPaths.stream().filter(Objects::nonNull).toList();
    }
}
