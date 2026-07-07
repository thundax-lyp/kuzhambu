package com.thundax.kuzhambu.operations.application.dashboard.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    void loadSummaryShouldCallAllDomainFacadesOnce() {
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
        Date periodStart = Date.from(Instant.parse("2026-06-01T00:00:00Z"));
        Date periodEnd = Date.from(Instant.parse("2026-06-30T23:59:59Z"));

        OperationsCrossDomainSummary result = gateway.loadSummary(periodStart, periodEnd, "WEEK");

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
        assertSame(periodStart, classicsCaptor.getValue().getPeriodStart());
        assertSame(periodEnd, classicsCaptor.getValue().getPeriodEnd());
        assertSame(periodStart, aiCaptor.getValue().getPeriodStart());
        assertSame(periodEnd, aiCaptor.getValue().getPeriodEnd());
        assertSame(periodStart, discoveryCaptor.getValue().getPeriodStart());
        assertSame(periodEnd, discoveryCaptor.getValue().getPeriodEnd());
        assertSame(periodStart, knowledgeCaptor.getValue().getPeriodStart());
        assertSame(periodEnd, knowledgeCaptor.getValue().getPeriodEnd());
        assertEquals("WEEK", classicsCaptor.getValue().getBucketType());
        assertEquals("WEEK", aiCaptor.getValue().getBucketType());
        assertEquals("WEEK", discoveryCaptor.getValue().getBucketType());
        assertEquals("WEEK", knowledgeCaptor.getValue().getBucketType());
    }

    @Test
    void loadSummaryShouldRejectMissingDomainSummary() {
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        when(classicsFacade.summary(any())).thenReturn(null);

        DefaultOperationsDashboardSummaryGateway gateway = new DefaultOperationsDashboardSummaryGateway(
                classicsFacade, mock(AiFacade.class), mock(DiscoveryFacade.class), mock(KnowledgeFacade.class));

        assertThrows(NullPointerException.class, () -> gateway.loadSummary(null, null, "DAY"));
    }
}
