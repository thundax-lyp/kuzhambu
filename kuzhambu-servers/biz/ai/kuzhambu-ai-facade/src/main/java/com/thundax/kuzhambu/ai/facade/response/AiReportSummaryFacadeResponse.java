package com.thundax.kuzhambu.ai.facade.response;

import com.thundax.kuzhambu.ai.facade.dto.AiTopCapabilityFacadeDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiReportSummaryFacadeResponse {

    private final Instant periodStart;
    private final Instant periodEnd;
    private final Long invocationCount;
    private final Long succeededInvocationCount;
    private final Long failedInvocationCount;
    private final Long avgLatencyMs;
    private final BigDecimal totalCostAmount;
    private final List<AiTopCapabilityFacadeDto> topCapabilities;
}
