package com.thundax.kuzhambu.operations.interfaces.admin.health.assembler;

import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthQuery;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthTrendQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthPageResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthTrendResult;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthTrendRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthPageResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthSummaryResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthTrendResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class OperationsHealthInterfaceAssembler {

    private OperationsHealthInterfaceAssembler() {}

    @NonNull
    public static OperationsHealthQuery toQuery(@NonNull OperationsHealthPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsHealthQuery(
                request.getComponent(),
                request.getHealthStatus(),
                request.getProbeSource(),
                request.getProbeTarget(),
                request.getCheckedAtStart(),
                request.getCheckedAtEnd());
    }

    @NonNull
    public static OperationsHealthTrendQuery toQuery(@NonNull OperationsHealthTrendRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsHealthTrendQuery(
                request.getComponent(),
                request.getProbeSource(),
                request.getPeriodStart(),
                request.getPeriodEnd(),
                request.getBucketType());
    }

    @NonNull
    public static OperationsHealthSummaryResponse toResponse(@NonNull OperationsHealthSummaryResult result) {
        Objects.requireNonNull(result, "result must not be null");
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

    @NonNull
    public static OperationsHealthPageResponse toResponse(@NonNull OperationsHealthPageResult result) {
        Objects.requireNonNull(result, "result must not be null");
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

    @NonNull
    public static OperationsHealthTrendResponse toResponse(@NonNull OperationsHealthTrendResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsHealthTrendResponse.builder()
                .bucket(result.getBucket())
                .upCount(result.getUpCount())
                .degradedCount(result.getDegradedCount())
                .downCount(result.getDownCount())
                .avgLatencyMs(result.getAvgLatencyMs())
                .build();
    }
}
