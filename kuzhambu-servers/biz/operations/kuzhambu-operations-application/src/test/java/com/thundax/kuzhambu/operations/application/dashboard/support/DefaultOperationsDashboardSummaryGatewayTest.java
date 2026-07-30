package com.thundax.kuzhambu.operations.application.dashboard.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.DiscoveryFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySummaryFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySummaryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSummaryFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DefaultOperationsDashboardSummaryGatewayTest {

    @Test
    void loadSummaryShouldCallEnabledFacadesOnce() {
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        DiscoveryFacade discoveryFacade = mock(DiscoveryFacade.class);
        KnowledgeFacade knowledgeFacade = mock(KnowledgeFacade.class);
        ClassicsSummaryFacadeResponse classicsSummary =
                ClassicsSummaryFacadeResponse.builder().contentCount(12L).build();
        AiReportSummaryFacadeResponse aiSummary =
                AiReportSummaryFacadeResponse.builder().invocationCount(9L).build();
        DiscoverySummaryFacadeResponse discoverySummary =
                DiscoverySummaryFacadeResponse.builder().searchCount(6L).build();
        KnowledgeSummaryFacadeResponse knowledgeSummary =
                KnowledgeSummaryFacadeResponse.builder().topTags(List.of()).build();
        when(classicsFacade.summary(any())).thenReturn(classicsSummary);
        when(aiFacade.summary(any())).thenReturn(aiSummary);
        when(discoveryFacade.summary(any())).thenReturn(discoverySummary);
        when(knowledgeFacade.summary(any())).thenReturn(knowledgeSummary);
        DefaultOperationsDashboardSummaryGateway gateway = new DefaultOperationsDashboardSummaryGateway(
                classicsFacade, aiFacade, discoveryFacade, knowledgeFacade);
        Instant periodStart = Instant.parse("2026-06-01T00:00:00Z");
        Instant periodEnd = Instant.parse("2026-06-30T23:59:59Z");
        OperationsDashboardPermissionSnapshot permissions =
                new OperationsDashboardPermissionSnapshot(true, true, true, true, true, true, true, true);

        OperationsCrossDomainSummary result = gateway.loadSummary(periodStart, periodEnd, "WEEK", permissions);

        assertSame(classicsSummary, result.classicsSummary());
        assertSame(aiSummary, result.aiSummary());
        assertSame(discoverySummary, result.discoverySummary());
        assertSame(knowledgeSummary, result.knowledgeSummary());
        ArgumentCaptor<ClassicsSummaryFacadeRequest> classicsCaptor =
                ArgumentCaptor.forClass(ClassicsSummaryFacadeRequest.class);
        ArgumentCaptor<AiReportSummaryFacadeRequest> aiCaptor =
                ArgumentCaptor.forClass(AiReportSummaryFacadeRequest.class);
        ArgumentCaptor<DiscoverySummaryFacadeRequest> discoveryCaptor =
                ArgumentCaptor.forClass(DiscoverySummaryFacadeRequest.class);
        ArgumentCaptor<KnowledgeSummaryFacadeRequest> knowledgeCaptor =
                ArgumentCaptor.forClass(KnowledgeSummaryFacadeRequest.class);
        verify(classicsFacade).summary(classicsCaptor.capture());
        verify(aiFacade).summary(aiCaptor.capture());
        verify(discoveryFacade).summary(discoveryCaptor.capture());
        verify(knowledgeFacade).summary(knowledgeCaptor.capture());
        assertEquals(Date.from(periodStart), classicsCaptor.getValue().getPeriodStart());
        assertEquals(Date.from(periodEnd), classicsCaptor.getValue().getPeriodEnd());
        assertEquals(periodStart, aiCaptor.getValue().getPeriodStart());
        assertEquals(periodEnd, aiCaptor.getValue().getPeriodEnd());
        assertEquals(periodStart, discoveryCaptor.getValue().getPeriodStart());
        assertEquals(periodEnd, discoveryCaptor.getValue().getPeriodEnd());
        assertEquals(periodStart, knowledgeCaptor.getValue().getPeriodStart());
        assertEquals(periodEnd, knowledgeCaptor.getValue().getPeriodEnd());
        assertEquals("WEEK", classicsCaptor.getValue().getBucketType());
        assertEquals("WEEK", aiCaptor.getValue().getBucketType());
        assertEquals("WEEK", discoveryCaptor.getValue().getBucketType());
        assertEquals("WEEK", knowledgeCaptor.getValue().getBucketType());
    }

    @Test
    void loadSummaryShouldSkipDisabledDomainFacades() {
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        DiscoveryFacade discoveryFacade = mock(DiscoveryFacade.class);
        KnowledgeFacade knowledgeFacade = mock(KnowledgeFacade.class);
        DefaultOperationsDashboardSummaryGateway gateway = new DefaultOperationsDashboardSummaryGateway(
                classicsFacade, aiFacade, discoveryFacade, knowledgeFacade);
        OperationsDashboardPermissionSnapshot permissions =
                new OperationsDashboardPermissionSnapshot(false, false, false, false, false, false, false, false);

        OperationsCrossDomainSummary result = gateway.loadSummary(
                Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-30T23:59:59Z"), "DAY", permissions);

        assertSame(null, result.classicsSummary());
        assertSame(null, result.aiSummary());
        assertSame(null, result.discoverySummary());
        assertSame(null, result.knowledgeSummary());
        verifyNoInteractions(classicsFacade, aiFacade, discoveryFacade, knowledgeFacade);
    }

    @Test
    void loadSummaryShouldOnlyCallDiscoveryFacadeForDiscoveryPermission() {
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        DiscoveryFacade discoveryFacade = mock(DiscoveryFacade.class);
        KnowledgeFacade knowledgeFacade = mock(KnowledgeFacade.class);
        when(discoveryFacade.summary(any()))
                .thenReturn(
                        DiscoverySummaryFacadeResponse.builder().searchCount(6L).build());
        DefaultOperationsDashboardSummaryGateway gateway = new DefaultOperationsDashboardSummaryGateway(
                classicsFacade, aiFacade, discoveryFacade, knowledgeFacade);
        OperationsDashboardPermissionSnapshot permissions =
                new OperationsDashboardPermissionSnapshot(false, false, true, false, false, false, false, false);

        OperationsCrossDomainSummary result = gateway.loadSummary(
                Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-30T23:59:59Z"), "DAY", permissions);

        assertSame(6L, result.discoverySummary().getSearchCount());
        assertSame(null, result.classicsSummary());
        assertSame(null, result.aiSummary());
        assertSame(null, result.knowledgeSummary());
        verify(discoveryFacade).summary(any());
        verify(classicsFacade, never()).summary(any());
        verify(aiFacade, never()).summary(any());
        verify(knowledgeFacade, never()).summary(any());
    }

    @Test
    void loadSummaryShouldSkipCrossDomainFacadesWhenNoChartPermission() {
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        DiscoveryFacade discoveryFacade = mock(DiscoveryFacade.class);
        KnowledgeFacade knowledgeFacade = mock(KnowledgeFacade.class);
        DefaultOperationsDashboardSummaryGateway gateway = new DefaultOperationsDashboardSummaryGateway(
                classicsFacade, aiFacade, discoveryFacade, knowledgeFacade);
        OperationsDashboardPermissionSnapshot permissions =
                new OperationsDashboardPermissionSnapshot(false, false, false, false, false, false, false, true);

        OperationsCrossDomainSummary result = gateway.loadSummary(
                Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-30T23:59:59Z"), "DAY", permissions);

        assertSame(null, result.classicsSummary());
        assertSame(null, result.aiSummary());
        assertSame(null, result.discoverySummary());
        assertSame(null, result.knowledgeSummary());
        verifyNoInteractions(classicsFacade, aiFacade, discoveryFacade, knowledgeFacade);
    }

    @Test
    void loadSummaryShouldRejectMissingEnabledDomainSummary() {
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        when(classicsFacade.summary(any())).thenReturn(null);
        OperationsDashboardPermissionSnapshot permissions =
                new OperationsDashboardPermissionSnapshot(true, false, false, false, false, false, false, false);
        DefaultOperationsDashboardSummaryGateway gateway = new DefaultOperationsDashboardSummaryGateway(
                classicsFacade, mock(AiFacade.class), mock(DiscoveryFacade.class), mock(KnowledgeFacade.class));

        assertThrows(
                NullPointerException.class,
                () -> gateway.loadSummary(
                        Instant.parse("2026-06-01T00:00:00Z"),
                        Instant.parse("2026-06-30T23:59:59Z"),
                        "DAY",
                        permissions));
    }
}
