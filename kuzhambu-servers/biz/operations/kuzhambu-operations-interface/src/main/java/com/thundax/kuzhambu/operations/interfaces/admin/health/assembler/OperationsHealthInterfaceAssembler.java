package com.thundax.kuzhambu.operations.interfaces.admin.health.assembler;

import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthPageQuery;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthTrendQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthPageResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthTrendResult;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthTrendRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthPageResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthSummaryResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthTrendResponse;

public final class OperationsHealthInterfaceAssembler {

    private OperationsHealthInterfaceAssembler() {}

    public static OperationsHealthPageQuery toQuery(OperationsHealthPageRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsHealthPageQuery(
                request.getComponent(),
                request.getHealthStatus(),
                request.getProbeSource(),
                request.getProbeTarget(),
                request.getCheckedAtStart(),
                request.getCheckedAtEnd());
    }

    public static OperationsHealthTrendQuery toQuery(OperationsHealthTrendRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsHealthTrendQuery(
                request.getComponent(),
                request.getProbeSource(),
                request.getPeriodStart(),
                request.getPeriodEnd(),
                request.getBucketType());
    }

    public static OperationsHealthSummaryResponse toResponse(OperationsHealthSummaryResult result) {
        if (result == null) {
            return null;
        }
        return OperationsHealthSummaryResponse.builder()
                .checkId(
                        result.getCheckId() == null ? null : result.getCheckId().value())
                .component(result.getComponent())
                .healthStatus(result.getHealthStatus())
                .latencyMs(result.getLatencyMs())
                .message(result.getMessage())
                .probeSource(result.getProbeSource())
                .probeTarget(result.getProbeTarget())
                .checkedAt(result.getCheckedAt())
                .build();
    }

    public static OperationsHealthPageResponse toResponse(OperationsHealthPageResult result) {
        if (result == null) {
            return null;
        }
        return OperationsHealthPageResponse.builder()
                .checkId(
                        result.getCheckId() == null ? null : result.getCheckId().value())
                .component(result.getComponent())
                .healthStatus(result.getHealthStatus())
                .latencyMs(result.getLatencyMs())
                .message(result.getMessage())
                .probeSource(result.getProbeSource())
                .probeTarget(result.getProbeTarget())
                .detailsJson(result.getDetailsJson())
                .checkedAt(result.getCheckedAt())
                .build();
    }

    public static OperationsHealthTrendResponse toResponse(OperationsHealthTrendResult result) {
        if (result == null) {
            return null;
        }
        return OperationsHealthTrendResponse.builder()
                .bucket(result.getBucket())
                .upCount(result.getUpCount())
                .degradedCount(result.getDegradedCount())
                .downCount(result.getDownCount())
                .avgLatencyMs(result.getAvgLatencyMs())
                .build();
    }
}
