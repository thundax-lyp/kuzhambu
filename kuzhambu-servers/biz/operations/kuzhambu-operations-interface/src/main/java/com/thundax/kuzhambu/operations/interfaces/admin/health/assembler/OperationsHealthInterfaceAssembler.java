package com.thundax.kuzhambu.operations.interfaces.admin.health.assembler;

import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthPageQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthPageResult;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthPageResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthSummaryResponse;

public final class OperationsHealthInterfaceAssembler {

    private OperationsHealthInterfaceAssembler() {}

    public static OperationsHealthPageQuery toQuery(OperationsHealthPageRequest request) {
        if (request == null) {
            return null;
        }
        return new OperationsHealthPageQuery(request.getComponent(), request.getHealthStatus());
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
                .checkedAt(result.getCheckedAt())
                .build();
    }
}
