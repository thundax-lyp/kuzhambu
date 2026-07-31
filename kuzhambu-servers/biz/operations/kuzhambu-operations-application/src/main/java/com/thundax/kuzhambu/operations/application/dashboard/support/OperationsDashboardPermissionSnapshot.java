package com.thundax.kuzhambu.operations.application.dashboard.support;

public record OperationsDashboardPermissionSnapshot(
        boolean canViewClassicsContentSummary,
        boolean canViewDiscoverySearchSummary,
        boolean canViewDiscoveryQaSummary,
        boolean canViewAiInvocationSummary,
        boolean canViewKnowledgeTaxonomySummary,
        boolean canViewHealthSummary,
        boolean canViewTaskSummary) {

    public boolean canLoadClassicsSummary() {
        return canViewClassicsContentSummary;
    }

    public boolean canLoadDiscoverySummary() {
        return canViewDiscoverySearchSummary || canViewDiscoveryQaSummary;
    }

    public boolean canLoadAiSummary() {
        return canViewAiInvocationSummary;
    }

    public boolean canLoadKnowledgeSummary() {
        return canViewKnowledgeTaxonomySummary;
    }

    public boolean hasAnyChartPermission() {
        return canViewClassicsContentSummary
                || canViewDiscoverySearchSummary
                || canViewDiscoveryQaSummary
                || canViewAiInvocationSummary
                || canViewKnowledgeTaxonomySummary
                || canViewHealthSummary
                || canViewTaskSummary;
    }
}
