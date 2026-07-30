package com.thundax.kuzhambu.operations.application.dashboard.support;

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
    public OperationsCrossDomainSummary loadSummary(
            Date periodStart, Date periodEnd, String bucketType, OperationsDashboardPermissionSnapshot permissions) {
        return new OperationsCrossDomainSummary(
                loadClassicsSummary(periodStart, periodEnd, bucketType, permissions),
                loadAiSummary(periodStart, periodEnd, bucketType, permissions),
                loadDiscoverySummary(periodStart, periodEnd, bucketType, permissions),
                loadKnowledgeSummary(periodStart, periodEnd, bucketType, permissions));
    }

    private ClassicsSummaryFacadeResponse loadClassicsSummary(
            Date periodStart, Date periodEnd, String bucketType, OperationsDashboardPermissionSnapshot permissions) {
        if (!permissions.canLoadClassicsSummary()) {
            return null;
        }
        return requireSummary(
                classicsFacade.summary(ClassicsSummaryFacadeRequest.builder()
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .bucketType(bucketType)
                        .build()),
                "classics");
    }

    private AiReportSummaryFacadeResponse loadAiSummary(
            Date periodStart, Date periodEnd, String bucketType, OperationsDashboardPermissionSnapshot permissions) {
        if (!permissions.canLoadAiSummary()) {
            return null;
        }
        return requireSummary(
                aiFacade.summary(AiReportSummaryFacadeRequest.builder()
                        .periodStart(periodStart == null ? null : periodStart.toInstant())
                        .periodEnd(periodEnd == null ? null : periodEnd.toInstant())
                        .bucketType(bucketType)
                        .build()),
                "ai");
    }

    private DiscoverySummaryFacadeResponse loadDiscoverySummary(
            Date periodStart, Date periodEnd, String bucketType, OperationsDashboardPermissionSnapshot permissions) {
        if (!permissions.canLoadDiscoverySummary()) {
            return null;
        }
        return requireSummary(
                discoveryFacade.summary(DiscoverySummaryFacadeRequest.builder()
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .bucketType(bucketType)
                        .build()),
                "discovery");
    }

    private KnowledgeSummaryFacadeResponse loadKnowledgeSummary(
            Date periodStart, Date periodEnd, String bucketType, OperationsDashboardPermissionSnapshot permissions) {
        if (!permissions.canLoadKnowledgeSummary()) {
            return null;
        }
        return requireSummary(
                knowledgeFacade.summary(KnowledgeSummaryFacadeRequest.builder()
                        .periodStart(periodStart == null ? null : periodStart.toInstant())
                        .periodEnd(periodEnd == null ? null : periodEnd.toInstant())
                        .bucketType(bucketType)
                        .build()),
                "knowledge");
    }

    private <T> T requireSummary(T summary, String domain) {
        return Objects.requireNonNull(summary, domain + " summary is required");
    }
}
