package com.thundax.kuzhambu.ai.domain.config.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {

    private PromptTemplateId id;
    private AiBusinessCapability capability;
    private String name;
    private String description;
    private boolean enabled = true;
    private Integer currentVersionNo;
    private Instant registeredAt;

    public boolean matches(String targetCapability) {
        return capability != null && capability.value().equals(targetCapability);
    }
}
