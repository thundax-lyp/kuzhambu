package com.thundax.kuzhambu.discovery.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.application.facade.assembler.DiscoveryFacadeAssembler;
import com.thundax.kuzhambu.discovery.application.report.result.DiscoveryReportSummaryResult;
import com.thundax.kuzhambu.discovery.application.report.service.DiscoveryReportApplicationService;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySummaryFacadeRequest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscoveryFacadeImplTest {

    @Test
    void summaryShouldDelegateAndMapFacadeResponse() {
        DiscoveryReportApplicationService discoveryReportApplicationService =
                mock(DiscoveryReportApplicationService.class);
        Instant periodStart = Instant.ofEpochMilli(1_735_689_600_000L);
        Instant periodEnd = Instant.ofEpochMilli(1_735_776_000_000L);
        when(discoveryReportApplicationService.summary(periodStart, periodEnd, "WEEK"))
                .thenReturn(new DiscoveryReportSummaryResult(
                        periodStart,
                        periodEnd,
                        18L,
                        6L,
                        240L,
                        List.of(new DiscoveryReportSummaryResult.TopQueryResult("青花瓷", 5L)),
                        List.of(new DiscoveryReportSummaryResult.SearchTrendPointResult("2025-W01", 9L)),
                        List.of(new DiscoveryReportSummaryResult.QaTrendPointResult("2025-W01", 3L))));
        DiscoveryFacadeImpl facade =
                new DiscoveryFacadeImpl(discoveryReportApplicationService, new DiscoveryFacadeAssembler());

        var response = facade.summary(DiscoverySummaryFacadeRequest.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .bucketType("WEEK")
                .build());

        assertEquals(periodStart, response.getPeriodStart());
        assertEquals(periodEnd, response.getPeriodEnd());
        assertEquals(18L, response.getSearchCount());
        assertEquals(6L, response.getQaCount());
        assertEquals(240L, response.getAvgSearchLatencyMs());
        assertEquals("青花瓷", response.getTopQueries().get(0).getQueryText());
        assertEquals("2025-W01", response.getSearchTrendSeries().get(0).getBucket());
        assertEquals(3L, response.getQaTrendSeries().get(0).getQaCount());
    }

    @Test
    void nullRequestShouldKeepFacadeBoundaryStable() {
        DiscoveryReportApplicationService discoveryReportApplicationService =
                mock(DiscoveryReportApplicationService.class);
        DiscoveryFacadeImpl facade =
                new DiscoveryFacadeImpl(discoveryReportApplicationService, new DiscoveryFacadeAssembler());

        assertNull(facade.summary(null));

        verifyNoInteractions(discoveryReportApplicationService);
    }
}
