package com.thundax.kuzhambu.ai.domain.model.model.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiModel {

    private Long id;
    private Long modelId;
    private Long serviceId;
    private String modelName;
    private String displayName;
    private List<String> capabilityTags = new ArrayList<>();
    private String defaultParamsJson;
    private String description;
    private boolean enabled = true;
    private Instant registeredAt;

    public boolean supportsAll(List<String> requiredTags) {
        if (requiredTags == null || requiredTags.isEmpty()) {
            return true;
        }
        return capabilityTags != null && capabilityTags.containsAll(requiredTags);
    }

    public boolean canBeMappedTo(List<String> requiredTags) {
        return enabled && supportsAll(requiredTags);
    }
}
