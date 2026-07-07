package com.thundax.kuzhambu.operations.application.dashboard.support;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.AiReportSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.DiscoveryFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySummaryFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSummaryFacadeRequest;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import java.util.Date;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class DefaultOperationsDashboardSummaryGateway implements OperationsDashboardSummaryGateway {

    private final ClassicsFacade classicsFacade;
    private final AiFacade aiFacade;
    private final DiscoveryFacade discoveryFacade;
    private final KnowledgeFacade knowledgeFacade;

    public DefaultOperationsDashboardSummaryGateway(
            ClassicsFacade classicsFacade,
            AiFacade aiFacade,
            DiscoveryFacade discoveryFacade,
            KnowledgeFacade knowledgeFacade) {
        this.classicsFacade = classicsFacade;
        this.aiFacade = aiFacade;
        this.discoveryFacade = discoveryFacade;
        this.knowledgeFacade = knowledgeFacade;
    }

    @Override
    public OperationsCrossDomainSummary loadSummary(Date periodStart, Date periodEnd, String bucketType) {
        return new OperationsCrossDomainSummary(
                requireSummary(
                        classicsFacade.summary(ClassicsSummaryFacadeRequest.builder()
                                .periodStart(periodStart)
                                .periodEnd(periodEnd)
                                .bucketType(bucketType)
                                .build()),
                        "classics"),
                requireSummary(
                        aiFacade.summary(AiReportSummaryFacadeRequest.builder()
                                .periodStart(periodStart)
                                .periodEnd(periodEnd)
                                .bucketType(bucketType)
                                .build()),
                        "ai"),
                requireSummary(
                        discoveryFacade.summary(DiscoverySummaryFacadeRequest.builder()
                                .periodStart(periodStart)
                                .periodEnd(periodEnd)
                                .bucketType(bucketType)
                                .build()),
                        "discovery"),
                requireSummary(
                        knowledgeFacade.summary(KnowledgeSummaryFacadeRequest.builder()
                                .periodStart(periodStart)
                                .periodEnd(periodEnd)
                                .bucketType(bucketType)
                                .build()),
                        "knowledge"));
    }

    private <T> T requireSummary(T summary, String domain) {
        return Objects.requireNonNull(summary, domain + " summary is required");
    }
}
