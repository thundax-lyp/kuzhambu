package com.thundax.kuzhambu.ai.application.invocation.result;

import java.math.BigDecimal;
import java.util.Date;
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

    private Date periodStart;
    private Date periodEnd;
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

        private String capability;
        private Long invocationCount;
    }
}
