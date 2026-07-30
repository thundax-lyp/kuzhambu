package com.thundax.kuzhambu.operations.application.report.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySummaryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardPermissionSnapshot;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryGateway;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSection;
import com.thundax.kuzhambu.operations.domain.report.codec.ReportIdCodec;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultOperationsReportMetricsGatewayTest {

    @Test
    void loadSectionsShouldWrapSharedSummaryAndResolveWeekBucketForMonthlyReport() {
        OperationsDashboardSummaryGateway summaryGateway = mock(OperationsDashboardSummaryGateway.class);
        OperationsDashboardPermissionSnapshot permissions = permissionSnapshotWithAllPrivileges();
        ClassicsSummaryFacadeResponse classicsSummary =
                ClassicsSummaryFacadeResponse.builder().contentCount(12L).build();
        DiscoverySummaryFacadeResponse discoverySummary =
                DiscoverySummaryFacadeResponse.builder().searchCount(6L).build();
        KnowledgeSummaryFacadeResponse knowledgeSummary = KnowledgeSummaryFacadeResponse.builder()
                .tagCoverageRate(new BigDecimal("0.75"))
                .build();
        AiReportSummaryFacadeResponse aiSummary = AiReportSummaryFacadeResponse.builder()
                .periodStart(Instant.parse("2026-06-01T00:00:00Z"))
                .periodEnd(Instant.parse("2026-06-30T23:59:59Z"))
                .invocationCount(9L)
                .succeededInvocationCount(7L)
                .failedInvocationCount(2L)
                .avgLatencyMs(230L)
                .totalCostAmount(new BigDecimal("8.88"))
                .build();
        when(summaryGateway.loadSummary(
                        monthlyRecord().getPeriodStart(), monthlyRecord().getPeriodEnd(), "WEEK", permissions))
                .thenReturn(new OperationsCrossDomainSummary(
                        classicsSummary, aiSummary, discoverySummary, knowledgeSummary));
        DefaultOperationsReportMetricsGateway gateway = new DefaultOperationsReportMetricsGateway(summaryGateway);

        List<OperationsReportSection> sections = gateway.loadSections(monthlyRecord());

        assertEquals(4, sections.size());
        assertEquals("classicsSummary", sections.get(0).getSectionKey());
        assertSame(classicsSummary, sections.get(0).getPayload().get("summary"));
        assertEquals("aiSummary", sections.get(1).getSectionKey());
        assertSame(aiSummary, sections.get(1).getPayload().get("summary"));
        assertEquals("discoverySummary", sections.get(2).getSectionKey());
        assertSame(discoverySummary, sections.get(2).getPayload().get("summary"));
        assertEquals("knowledgeSummary", sections.get(3).getSectionKey());
        assertSame(knowledgeSummary, sections.get(3).getPayload().get("summary"));
        verify(summaryGateway)
                .loadSummary(monthlyRecord().getPeriodStart(), monthlyRecord().getPeriodEnd(), "WEEK", permissions);
    }

    @Test
    void loadSectionsShouldResolveDayBucketForNonMonthlyReport() {
        OperationsDashboardSummaryGateway summaryGateway = mock(OperationsDashboardSummaryGateway.class);
        OperationsDashboardPermissionSnapshot permissions = permissionSnapshotWithAllPrivileges();
        when(summaryGateway.loadSummary(
                        weeklyRecord().getPeriodStart(), weeklyRecord().getPeriodEnd(), "DAY", permissions))
                .thenReturn(new OperationsCrossDomainSummary(
                        ClassicsSummaryFacadeResponse.builder().build(),
                        AiReportSummaryFacadeResponse.builder().build(),
                        DiscoverySummaryFacadeResponse.builder().build(),
                        KnowledgeSummaryFacadeResponse.builder().build()));
        DefaultOperationsReportMetricsGateway gateway = new DefaultOperationsReportMetricsGateway(summaryGateway);

        gateway.loadSections(weeklyRecord());

        verify(summaryGateway)
                .loadSummary(weeklyRecord().getPeriodStart(), weeklyRecord().getPeriodEnd(), "DAY", permissions);
    }

    private static OperationsDashboardPermissionSnapshot permissionSnapshotWithAllPrivileges() {
        return new OperationsDashboardPermissionSnapshot(true, true, true, true, true, true, true, true);
    }

    private static ReportRecord monthlyRecord() {
        return new ReportRecord(
                ReportIdCodec.toDomain(7001L),
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
                ReportIdCodec.toDomain(7002L),
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
