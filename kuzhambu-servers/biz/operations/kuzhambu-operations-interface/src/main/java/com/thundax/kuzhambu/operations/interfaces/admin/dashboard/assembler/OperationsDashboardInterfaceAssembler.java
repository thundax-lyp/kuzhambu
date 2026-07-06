package com.thundax.kuzhambu.operations.interfaces.admin.dashboard.assembler;

import com.thundax.kuzhambu.operations.application.dashboard.query.OperationsDashboardOverviewQuery;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.BucketCountResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TaskStatusSummaryResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TopAiCapabilityResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TopContentResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TopQueryResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TopTagResult;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.request.OperationsDashboardOverviewRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response.OperationsDashboardOverviewResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response.OperationsDashboardOverviewResponse.BucketCountResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response.OperationsDashboardOverviewResponse.TaskStatusSummaryResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response.OperationsDashboardOverviewResponse.TopAiCapabilityResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response.OperationsDashboardOverviewResponse.TopContentResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response.OperationsDashboardOverviewResponse.TopQueryResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.response.OperationsDashboardOverviewResponse.TopTagResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.assembler.OperationsHealthInterfaceAssembler;
import java.util.List;

public final class OperationsDashboardInterfaceAssembler {

    private OperationsDashboardInterfaceAssembler() {}

    public static OperationsDashboardOverviewQuery toQuery(OperationsDashboardOverviewRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsDashboardOverviewQuery(
                request.getPeriodType(), request.getPeriodStart(), request.getPeriodEnd());
    }

    public static OperationsDashboardOverviewResponse toResponse(OperationsDashboardOverviewResult result) {
        if (result == null) {
            return null;
        }
        return OperationsDashboardOverviewResponse.builder()
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .contentCount(result.getContentCount())
                .translatedContentCount(result.getTranslatedContentCount())
                .imageReadyContentCount(result.getImageReadyContentCount())
                .visualAssetReadyContentCount(result.getVisualAssetReadyContentCount())
                .shareVisitCount(result.getShareVisitCount())
                .aiInvocationCount(result.getAiInvocationCount())
                .aiSucceededInvocationCount(result.getAiSucceededInvocationCount())
                .aiFailedInvocationCount(result.getAiFailedInvocationCount())
                .aiAvgLatencyMs(result.getAiAvgLatencyMs())
                .aiTotalCostAmount(result.getAiTotalCostAmount())
                .searchCount(result.getSearchCount())
                .qaCount(result.getQaCount())
                .avgSearchLatencyMs(result.getAvgSearchLatencyMs())
                .tagCoverageRate(result.getTagCoverageRate())
                .unhealthyComponentCount(result.getUnhealthyComponentCount())
                .runningTaskCount(result.getRunningTaskCount())
                .failedTaskCount(result.getFailedTaskCount())
                .contentGrowthSeries(toBucketResponses(result.getContentGrowthSeries()))
                .searchTrendSeries(toBucketResponses(result.getSearchTrendSeries()))
                .qaTrendSeries(toBucketResponses(result.getQaTrendSeries()))
                .tagGrowthSeries(toBucketResponses(result.getTagGrowthSeries()))
                .healthSummaries(
                        result.getHealthSummaries() == null
                                ? List.of()
                                : result.getHealthSummaries().stream()
                                        .map(OperationsHealthInterfaceAssembler::toResponse)
                                        .toList())
                .taskStatusSummaries(
                        result.getTaskStatusSummaries() == null
                                ? List.of()
                                : result.getTaskStatusSummaries().stream()
                                        .map(OperationsDashboardInterfaceAssembler::toResponse)
                                        .toList())
                .topContents(
                        result.getTopContents() == null
                                ? List.of()
                                : result.getTopContents().stream()
                                        .map(OperationsDashboardInterfaceAssembler::toResponse)
                                        .toList())
                .topQueries(
                        result.getTopQueries() == null
                                ? List.of()
                                : result.getTopQueries().stream()
                                        .map(OperationsDashboardInterfaceAssembler::toResponse)
                                        .toList())
                .topTags(
                        result.getTopTags() == null
                                ? List.of()
                                : result.getTopTags().stream()
                                        .map(OperationsDashboardInterfaceAssembler::toResponse)
                                        .toList())
                .topAiCapabilities(
                        result.getTopAiCapabilities() == null
                                ? List.of()
                                : result.getTopAiCapabilities().stream()
                                        .map(OperationsDashboardInterfaceAssembler::toResponse)
                                        .toList())
                .build();
    }

    private static List<BucketCountResponse> toBucketResponses(List<BucketCountResult> results) {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .map(OperationsDashboardInterfaceAssembler::toResponse)
                .toList();
    }

    private static BucketCountResponse toResponse(BucketCountResult result) {
        if (result == null) {
            return null;
        }
        return BucketCountResponse.builder()
                .bucket(result.getBucket())
                .count(result.getCount())
                .build();
    }

    private static TaskStatusSummaryResponse toResponse(TaskStatusSummaryResult result) {
        if (result == null) {
            return null;
        }
        return TaskStatusSummaryResponse.builder()
                .taskStatus(result.getTaskStatus())
                .count(result.getCount())
                .build();
    }

    private static TopContentResponse toResponse(TopContentResult result) {
        if (result == null) {
            return null;
        }
        return TopContentResponse.builder()
                .contentId(result.getContentId())
                .contentType(result.getContentType())
                .title(result.getTitle())
                .visitCount(result.getVisitCount())
                .build();
    }

    private static TopQueryResponse toResponse(TopQueryResult result) {
        if (result == null) {
            return null;
        }
        return TopQueryResponse.builder()
                .queryText(result.getQueryText())
                .count(result.getCount())
                .build();
    }

    private static TopTagResponse toResponse(TopTagResult result) {
        if (result == null) {
            return null;
        }
        return TopTagResponse.builder()
                .tagName(result.getTagName())
                .contentRefCount(result.getContentRefCount())
                .build();
    }

    private static TopAiCapabilityResponse toResponse(TopAiCapabilityResult result) {
        if (result == null) {
            return null;
        }
        return TopAiCapabilityResponse.builder()
                .capability(result.getCapability())
                .invocationCount(result.getInvocationCount())
                .build();
    }
}
