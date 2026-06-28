package com.thundax.kuzhambu.operations.application.report.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.classics.application.report.service.ClassicsReportApplicationService;
import com.thundax.kuzhambu.discovery.application.report.service.DiscoveryReportApplicationService;
import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSection;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DefaultOperationsReportMetricsGatewayTest {

    @Test
    void loadSectionsShouldReadAiSummaryThroughFacadeAndResolveWeekBucketForMonthlyReport() {
        ClassicsReportApplicationService classicsReportApplicationService =
                mock(ClassicsReportApplicationService.class);
        DiscoveryReportApplicationService discoveryReportApplicationService =
                mock(DiscoveryReportApplicationService.class);
        KnowledgeFacade knowledgeFacade = mock(KnowledgeFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        AiReportSummaryFacadeResponse aiSummary = AiReportSummaryFacadeResponse.builder()
                .periodStart(Date.from(Instant.parse("2026-06-01T00:00:00Z")))
                .periodEnd(Date.from(Instant.parse("2026-06-30T23:59:59Z")))
                .invocationCount(9L)
                .succeededInvocationCount(7L)
                .failedInvocationCount(2L)
                .avgLatencyMs(230L)
                .totalCostAmount(new BigDecimal("8.88"))
                .build();
        when(aiFacade.summary(any())).thenReturn(aiSummary);
        DefaultOperationsReportMetricsGateway gateway = new DefaultOperationsReportMetricsGateway(
                classicsReportApplicationService, aiFacade, discoveryReportApplicationService, knowledgeFacade);

        List<OperationsReportSection> sections = gateway.loadSections(monthlyRecord());

        assertEquals(4, sections.size());
        assertEquals("classicsSummary", sections.get(0).getSectionKey());
        assertEquals("aiSummary", sections.get(1).getSectionKey());
        assertSame(aiSummary, sections.get(1).getPayload().get("summary"));
        assertEquals("discoverySummary", sections.get(2).getSectionKey());
        assertEquals("knowledgeSummary", sections.get(3).getSectionKey());

        ArgumentCaptor<AiReportSummaryFacadeRequest> captor =
                ArgumentCaptor.forClass(AiReportSummaryFacadeRequest.class);
        verify(aiFacade).summary(captor.capture());
        AiReportSummaryFacadeRequest request = captor.getValue();
        assertEquals(monthlyRecord().getPeriodStart(), request.getPeriodStart());
        assertEquals(monthlyRecord().getPeriodEnd(), request.getPeriodEnd());
        assertEquals("WEEK", request.getBucketType());
    }

    @Test
    void loadSectionsShouldResolveDayBucketForNonMonthlyReport() {
        AiFacade aiFacade = mock(AiFacade.class);
        when(aiFacade.summary(any()))
                .thenReturn(AiReportSummaryFacadeResponse.builder().build());
        DefaultOperationsReportMetricsGateway gateway = new DefaultOperationsReportMetricsGateway(
                mock(ClassicsReportApplicationService.class),
                aiFacade,
                mock(DiscoveryReportApplicationService.class),
                mock(KnowledgeFacade.class));

        gateway.loadSections(weeklyRecord());

        ArgumentCaptor<AiReportSummaryFacadeRequest> captor =
                ArgumentCaptor.forClass(AiReportSummaryFacadeRequest.class);
        verify(aiFacade).summary(captor.capture());
        assertEquals("DAY", captor.getValue().getBucketType());
    }

    private static ReportRecord monthlyRecord() {
        return new ReportRecord(
                ReportId.of(7001L),
                "MONTHLY",
                "PDF",
                Date.from(Instant.parse("2026-06-01T00:00:00Z")),
                Date.from(Instant.parse("2026-06-30T23:59:59Z")),
                "req-month",
                "trace-month",
                "2026.06",
                null,
                null,
                ReportStatus.PENDING,
                null,
                9001L,
                Date.from(Instant.parse("2026-07-01T00:00:00Z")),
                null);
    }

    private static ReportRecord weeklyRecord() {
        return new ReportRecord(
                ReportId.of(7002L),
                "WEEKLY",
                "PDF",
                Date.from(Instant.parse("2026-06-22T00:00:00Z")),
                Date.from(Instant.parse("2026-06-28T23:59:59Z")),
                "req-week",
                "trace-week",
                "2026.06.28",
                null,
                null,
                ReportStatus.PENDING,
                null,
                9002L,
                Date.from(Instant.parse("2026-06-29T00:00:00Z")),
                null);
    }
}
