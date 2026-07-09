package com.thundax.kuzhambu.ai.domain.capability.model.entity;

import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiCapabilityMapping {

    private Long id;
    private Long mappingId;
    private String scope;
    private String capability;
    private Long modelId;
    private boolean enabled = true;
    private Instant configuredAt;

    public boolean canUse(AiCapability capabilityDefinition, AiModel model) {
        if (!enabled || capabilityDefinition == null || model == null) {
            return false;
        }
        if (!model.isEnabled()) {
            return false;
        }
        return capabilityDefinition.isSatisfiedBy(model.getCapabilityTags());
    }
}
