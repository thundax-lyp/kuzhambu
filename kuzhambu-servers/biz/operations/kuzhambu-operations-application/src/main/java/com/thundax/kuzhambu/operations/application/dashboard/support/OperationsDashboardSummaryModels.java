package com.thundax.kuzhambu.operations.application.dashboard.support;

import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySummaryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;

public final class OperationsDashboardSummaryModels {

    private OperationsDashboardSummaryModels() {}

    public record OperationsCrossDomainSummary(
            ClassicsSummaryFacadeResponse classicsSummary,
            AiReportSummaryFacadeResponse aiSummary,
            DiscoverySummaryFacadeResponse discoverySummary,
            KnowledgeSummaryFacadeResponse knowledgeSummary) {}
}
