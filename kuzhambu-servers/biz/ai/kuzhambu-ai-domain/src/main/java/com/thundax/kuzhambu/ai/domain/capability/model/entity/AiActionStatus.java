package com.thundax.kuzhambu.ai.domain.capability.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiActionStatus {

    private Long id;
    private Long actionStatusId;
    private String scope;
    private String capability;
    private boolean available;
    private String unavailableReason;
    private Instant checkedAt;

    public static AiActionStatus available(Long actionStatusId, String scope, String capability, Instant checkedAt) {
        return new AiActionStatus(null, actionStatusId, scope, capability, true, null, checkedAt);
    }

    public static AiActionStatus unavailable(
            Long actionStatusId, String scope, String capability, String reason, Instant checkedAt) {
        return new AiActionStatus(null, actionStatusId, scope, capability, false, reason, checkedAt);
    }
}
