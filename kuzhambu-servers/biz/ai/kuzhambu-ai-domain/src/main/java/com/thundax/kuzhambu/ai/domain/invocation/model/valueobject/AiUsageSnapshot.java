package com.thundax.kuzhambu.ai.domain.invocation.model.valueobject;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageSnapshot {

    private Integer latencyMs = 0;
    private int inputTokens;
    private int outputTokens;
    private BigDecimal costAmount = BigDecimal.ZERO;

    public static AiUsageSnapshot empty() {
        return new AiUsageSnapshot(0, 0, 0, BigDecimal.ZERO);
    }

    public static AiUsageSnapshot orEmpty(AiUsageSnapshot usageSnapshot) {
        return usageSnapshot == null ? empty() : usageSnapshot;
    }
}
