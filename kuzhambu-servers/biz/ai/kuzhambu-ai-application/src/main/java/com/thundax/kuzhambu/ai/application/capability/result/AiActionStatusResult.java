package com.thundax.kuzhambu.ai.application.capability.result;

import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiActionStatusResult {

    private final String scope;
    private final String capability;
    private final boolean available;
    private final String unavailableReason;
    private final Instant checkedAt;

    public static AiActionStatusResult from(AiActionStatus status) {
        if (status == null) {
            return null;
        }
        return new AiActionStatusResult(
                status.getScope(),
                status.getCapability(),
                status.isAvailable(),
                status.getUnavailableReason(),
                status.getCheckedAt());
    }
}
