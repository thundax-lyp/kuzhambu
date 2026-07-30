package com.thundax.kuzhambu.ai.application.invocation.result;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiReportSummaryResult {

    private Instant periodStart;
    private Instant periodEnd;
    private Long invocationCount;
    private Long succeededInvocationCount;
    private Long failedInvocationCount;
    private Long avgLatencyMs;
    private BigDecimal totalCostAmount;
    private List<TopCapabilityResult> topCapabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCapabilityResult {

        private AiBusinessCapability capability;
        private Long invocationCount;
    }
}
