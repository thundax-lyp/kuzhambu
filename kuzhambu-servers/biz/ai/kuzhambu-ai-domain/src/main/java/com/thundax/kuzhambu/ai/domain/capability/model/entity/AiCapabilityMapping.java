package com.thundax.kuzhambu.ai.domain.capability.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
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

    public boolean canUse(AiModel model) {
        if (!enabled || model == null) {
            return false;
        }
        AiBusinessCapability.from(capability);
        if (!model.isEnabled()) {
            return false;
        }
        return true;
    }
}
