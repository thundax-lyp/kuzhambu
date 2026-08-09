package com.thundax.kuzhambu.operations.interfaces.admin.health.assembler;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertAckCommand;
import com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertRecoverCommand;
import com.thundax.kuzhambu.operations.application.health.query.OperationsHealthAlertQuery;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthAlertPageResult;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthAlertIdCodec;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertAckRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request.OperationsHealthAlertRecoverRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response.OperationsHealthAlertPageResponse;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class OperationsHealthAlertInterfaceAssembler {

    private OperationsHealthAlertInterfaceAssembler() {}

    @NonNull
    public static OperationsHealthAlertQuery toQuery(@NonNull OperationsHealthAlertPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsHealthAlertQuery(
                request.getComponent(),
                request.getAlertLevel(),
                request.getAlertStatus(),
                request.getSourceRefType(),
                request.getSourceRefId(),
                request.getLatestCheckId());
    }

    @NonNull
    public static OperationsHealthAlertAckCommand toCommand(@NonNull OperationsHealthAlertAckRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsHealthAlertAckCommand(
                HealthAlertIdCodec.toDomain(request.getAlertId()), currentAdminUserId());
    }

    @NonNull
    public static OperationsHealthAlertRecoverCommand toCommand(@NonNull OperationsHealthAlertRecoverRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new OperationsHealthAlertRecoverCommand(
                HealthAlertIdCodec.toDomain(request.getAlertId()), currentAdminUserId());
    }

    @NonNull
    public static OperationsHealthAlertPageResponse toResponse(@NonNull OperationsHealthAlertPageResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return OperationsHealthAlertPageResponse.builder()
                .alertId(
                        result.getAlertId() == null ? null : result.getAlertId().value())
                .component(result.getComponent())
                .alertType(result.getAlertType())
                .alertLevel(result.getAlertLevel())
                .alertStatus(result.getAlertStatus())
                .sourceRefType(result.getSourceRefType())
                .sourceRefId(result.getSourceRefId())
                .latestCheckId(
                        result.getLatestCheckId() == null
                                ? null
                                : result.getLatestCheckId().value())
                .message(result.getMessage())
                .suggestion(result.getSuggestion())
                .recoveryAction(result.getRecoveryAction())
                .recoveryTarget(result.getRecoveryTarget())
                .firstTriggeredAt(result.getFirstTriggeredAt())
                .lastTriggeredAt(result.getLastTriggeredAt())
                .ackedAt(result.getAckedAt())
                .ackedByUserId(result.getAckedByUserId())
                .recoveredAt(result.getRecoveredAt())
                .failureReason(result.getFailureReason())
                .build();
    }

    private static Long currentAdminUserId() {
        if (KuzhambuContextHolder.currentSubjectType() != KuzhambuSubjectType.ADMIN_USER) {
            return null;
        }
        String subjectId = KuzhambuContextHolder.currentSubjectId();
        if (subjectId == null || subjectId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(subjectId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
