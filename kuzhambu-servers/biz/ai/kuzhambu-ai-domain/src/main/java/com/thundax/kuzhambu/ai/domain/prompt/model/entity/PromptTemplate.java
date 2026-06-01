package com.thundax.kuzhambu.ai.domain.prompt.model.entity;

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

    private Long id;
    private Long templateId;
    private String scope;
    private String capability;
    private String name;
    private String description;
    private String status = "ACTIVE";
    private Integer currentVersionNo;
    private Instant registeredAt;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean matches(String targetScope, String targetCapability) {
        return scope != null && scope.equals(targetScope) && capability != null && capability.equals(targetCapability);
    }
}
