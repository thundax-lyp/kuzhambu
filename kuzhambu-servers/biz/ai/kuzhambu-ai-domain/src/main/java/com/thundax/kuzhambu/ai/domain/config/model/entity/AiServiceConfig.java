package com.thundax.kuzhambu.ai.domain.config.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiServiceConfig {

    private Long id;
    private Long serviceId;
    private String serviceRole;
    private String apiSource;
    private String baseUrl;
    private String encryptedApiKey;
    private boolean enabled = true;
    private String status = "UNAVAILABLE";
    private Instant lastCheckedAt;
    private Instant configuredAt;

    public boolean isAvailable() {
        return enabled && "AVAILABLE".equals(status);
    }

    public boolean isPrimary() {
        return "PRIMARY".equals(serviceRole);
    }
}
