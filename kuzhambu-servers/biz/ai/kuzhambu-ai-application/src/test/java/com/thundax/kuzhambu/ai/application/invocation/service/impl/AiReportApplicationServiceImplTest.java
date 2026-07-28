package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.application.invocation.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiUsageSnapshot;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiReportApplicationServiceImplTest {

    @Test
    void summaryShouldAggregateInvocationOutcomeLatencyCostAndTopCapabilities() {
        AiInvocationRepository repository = mock(AiInvocationRepository.class);
        AiReportApplicationServiceImpl service = new AiReportApplicationServiceImpl(repository);
        when(repository.listInvocationLogs(any(), any()))
                .thenReturn(List.of(
                        invocationLog(
                                AiBusinessCapability.CLASSICS_TRANSLATE, AiInvocationStatus.SUCCEEDED, 120, "1.20"),
                        invocationLog(AiBusinessCapability.CLASSICS_TRANSLATE, AiInvocationStatus.FAILED, 300, "0.80"),
                        invocationLog(
                                AiBusinessCapability.KNOWLEDGE_GRAPH_EXTRACT,
                                AiInvocationStatus.SUCCEEDED,
                                180,
                                "2.00")));

        AiReportSummaryResult result = service.summary(
                Date.from(Instant.parse("2024-06-01T00:00:00Z")),
                Date.from(Instant.parse("2024-06-30T23:59:59Z")),
                "DAY");

        assertEquals(3L, result.getInvocationCount());
        assertEquals(2L, result.getSucceededInvocationCount());
        assertEquals(1L, result.getFailedInvocationCount());
        assertEquals(200L, result.getAvgLatencyMs());
        assertEquals(new BigDecimal("4.00"), result.getTotalCostAmount());
        assertEquals("classics_translate", result.getTopCapabilities().get(0).getCapability());
        assertEquals(2L, result.getTopCapabilities().get(0).getInvocationCount());
        assertEquals(
                "knowledge_graph_extract", result.getTopCapabilities().get(1).getCapability());
    }

    private static AiInvocationLog invocationLog(
            AiBusinessCapability capability, AiInvocationStatus status, int latencyMs, String costAmount) {
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setCapability(capability);
        invocationLog.setStatus(status);
        invocationLog.setUsage(new AiUsageSnapshot(latencyMs, 10, 20, new BigDecimal(costAmount)));
        return invocationLog;
    }
}
