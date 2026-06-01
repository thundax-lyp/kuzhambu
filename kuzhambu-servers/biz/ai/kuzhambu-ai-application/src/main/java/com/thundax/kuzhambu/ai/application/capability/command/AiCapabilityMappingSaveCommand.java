package com.thundax.kuzhambu.ai.application.capability.command;

import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCapabilityMappingSaveCommand {

    private Long mappingId;
    private String scope;
    private String capability;
    private Long modelId;
    private boolean enabled = true;
    private Instant configuredAt;

    public AiCapabilityMapping toEntity() {
        AiCapabilityMapping mapping = new AiCapabilityMapping();
        mapping.setMappingId(mappingId);
        mapping.setScope(scope);
        mapping.setCapability(capability);
        mapping.setModelId(modelId);
        mapping.setEnabled(enabled);
        mapping.setConfiguredAt(configuredAt == null ? Instant.now() : configuredAt);
        return mapping;
    }
}
