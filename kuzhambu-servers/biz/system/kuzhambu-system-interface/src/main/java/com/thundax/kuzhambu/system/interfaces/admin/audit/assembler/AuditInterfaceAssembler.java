package com.thundax.kuzhambu.system.interfaces.admin.audit.assembler;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditField;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssembler;
import com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssemblerRegistry;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.audit.query.AuditLogQuery;
import com.thundax.kuzhambu.system.application.audit.query.AuditMetaQuery;
import com.thundax.kuzhambu.system.application.audit.query.GetAuditLogQuery;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditLogIdCodec;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditMetaIdCodec;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditLogDetailRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditLogPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditMetaRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request.AuditObjectPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditFieldResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditLogDetailResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditLogResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditMetaResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditObjectFieldResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditObjectOverviewResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditOptionResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditOptionsResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditSnapshotFieldResponse;
import com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response.AuditSnapshotResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

public final class AuditInterfaceAssembler {

    private AuditInterfaceAssembler() {}

    @NonNull
    public static AuditMetaQuery toMetaQuery(@NonNull AuditMetaRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AuditMetaQuery(AuditObjectRef.of(request.getObjectType(), request.getObjectId()));
    }

    @NonNull
    public static AuditLogQuery toLogQuery(@NonNull AuditLogPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AuditLogQuery(
                AuditObjectRef.of(request.getObjectType(), request.getObjectId()),
                request.getAction() == null ? null : AuditAction.from(request.getAction()),
                AuditOperatorRef.of(
                        request.getOperatorType() == null ? null : AuditOperatorType.from(request.getOperatorType()),
                        request.getOperatorId()),
                request.getSource(),
                request.getRequestId(),
                request.getBeginDate(),
                request.getEndDate());
    }

    @NonNull
    public static GetAuditLogQuery toGetLogQuery(@NonNull AuditLogDetailRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new GetAuditLogQuery(AuditLogIdCodec.toDomain(request.getId()));
    }

    @NonNull
    public static AuditLogQuery toLogQuery(@NonNull AuditObjectPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AuditLogQuery(
                AuditObjectRef.of(request.getObjectType(), request.getObjectId()), null, null, null, null, null, null);
    }

    @NonNull
    public static AuditLogQuery toObjectLogQuery(@NonNull AuditMetaRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new AuditLogQuery(
                AuditObjectRef.of(request.getObjectType(), request.getObjectId()), null, null, null, null, null, null);
    }

    @NonNull
    public static AuditMetaResponse emptyMetaResponse() {
        return AuditMetaResponse.builder().build();
    }

    @NonNull
    public static AuditMetaResponse toMetaResponse(@NonNull AuditMeta entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return AuditMetaResponse.builder()
                .id(AuditMetaIdCodec.toStringValue(entity.getId()))
                .objectType(entity.getObjectType())
                .objectId(entity.getObjectId())
                .version(entity.getVersion())
                .lastAction(
                        entity.getLastAction() == null
                                ? null
                                : entity.getLastAction().value())
                .lastOperatorName(entity.getLastOperatorName())
                .lastOperatedAt(entity.getLastOperatedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @NonNull
    public static AuditLogResponse toLogResponse(@NonNull AuditLog entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return logResponseBuilder(entity, null).build();
    }

    @NonNull
    public static AuditLogResponse toLogResponse(
            @NonNull AuditLog entity, @NonNull AuditSnapshotAssemblerRegistry registry) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(registry, "registry must not be null");
        return logResponseBuilder(entity, registry).build();
    }

    @NonNull
    public static AuditLogDetailResponse emptyLogDetailResponse() {
        return AuditLogDetailResponse.builder().changedFields(new ArrayList<>()).build();
    }

    @NonNull
    public static AuditLogDetailResponse toLogDetailResponse(@NonNull AuditLog entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return logDetailResponse(entity, null);
    }

    @NonNull
    public static AuditLogDetailResponse toLogDetailResponse(
            @NonNull AuditLog entity, @NonNull AuditSnapshotAssemblerRegistry registry) {
        Objects.requireNonNull(entity, "entity must not be null");
        Objects.requireNonNull(registry, "registry must not be null");
        return AuditLogDetailResponse.builder()
                .id(AuditLogIdCodec.toStringValue(entity.getId()))
                .objectType(entity.getObjectType())
                .objectTypeLabel(objectTypeLabel(entity.getObjectType(), registry))
                .objectId(entity.getObjectId())
                .objectDisplayName(displayName(entity))
                .version(entity.getVersion())
                .action(entity.getAction() == null ? null : entity.getAction().value())
                .actionLabel(actionLabel(entity.getAction()))
                .operatorType(
                        entity.getOperatorType() == null
                                ? null
                                : entity.getOperatorType().value())
                .operatorTypeLabel(operatorTypeLabel(entity.getOperatorType()))
                .operatorId(entity.getOperatorId())
                .operatorName(entity.getOperatorName())
                .source(entity.getSource())
                .requestId(entity.getRequestId())
                .traceId(entity.getTraceId())
                .remoteAddr(entity.getRemoteAddr())
                .summary(entity.getSummary())
                .occurredAt(entity.getOccurredAt())
                .changedFields(toChangedFieldResponses(entity.getChangedFields()))
                .idempotencyKey(entity.getIdempotencyKey())
                .previousVersion(entity.getPreviousVersion())
                .beforeSnapshot(toSnapshotResponse(entity.getBeforeSnapshot()))
                .afterSnapshot(toSnapshotResponse(entity.getAfterSnapshot()))
                .build();
    }

    @NonNull
    public static AuditObjectOverviewResponse toOverviewResponse(
            @NonNull AuditMeta meta, @NonNull PageResult<AuditLog> latestLogs) {
        Objects.requireNonNull(meta, "meta must not be null");
        Objects.requireNonNull(latestLogs, "latestLogs must not be null");
        return AuditObjectOverviewResponse.builder()
                .meta(toMetaResponse(meta))
                .latestLogs(toLatestLogResponses(latestLogs, null))
                .build();
    }

    @NonNull
    public static AuditObjectOverviewResponse emptyOverviewResponse(@NonNull PageResult<AuditLog> latestLogs) {
        Objects.requireNonNull(latestLogs, "latestLogs must not be null");
        return AuditObjectOverviewResponse.builder()
                .meta(emptyMetaResponse())
                .latestLogs(toLatestLogResponses(latestLogs, null))
                .build();
    }

    @NonNull
    public static AuditObjectOverviewResponse toOverviewResponse(
            @NonNull AuditMeta meta,
            @NonNull PageResult<AuditLog> latestLogs,
            @NonNull AuditSnapshotAssemblerRegistry registry) {
        Objects.requireNonNull(meta, "meta must not be null");
        Objects.requireNonNull(latestLogs, "latestLogs must not be null");
        Objects.requireNonNull(registry, "registry must not be null");
        return AuditObjectOverviewResponse.builder()
                .meta(toMetaResponse(meta))
                .latestLogs(toLatestLogResponses(latestLogs, registry))
                .build();
    }

    @NonNull
    public static AuditOptionsResponse toOptionsResponse(@NonNull AuditSnapshotAssemblerRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");
        return AuditOptionsResponse.builder()
                .objectTypes(objectTypeOptions(registry))
                .actions(Arrays.stream(AuditAction.values())
                        .map(action -> option(action.value(), actionLabel(action)))
                        .collect(Collectors.toList()))
                .operatorTypes(Arrays.stream(AuditOperatorType.values())
                        .map(type -> option(type.value(), operatorTypeLabel(type)))
                        .collect(Collectors.toList()))
                .build();
    }

    private static List<AuditOptionResponse> objectTypeOptions(AuditSnapshotAssemblerRegistry registry) {
        if (registry == null) {
            return new ArrayList<>();
        }
        return registry.list().stream()
                .map(assembler -> option(assembler.objectType(), assembler.objectTypeLabel()))
                .collect(Collectors.toList());
    }

    @NonNull
    public static List<AuditObjectFieldResponse> toFieldResponses(
            @NonNull AuditSnapshotAssemblerRegistry registry, @NonNull String objectType) {
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(objectType, "objectType must not be null");
        AuditSnapshotAssembler assembler = registry.get(objectType);
        if (assembler == null || assembler.fields() == null) {
            return new ArrayList<>();
        }
        return assembler.fields().stream()
                .map(field -> objectField(field.getFieldName(), field.getFieldLabel()))
                .collect(Collectors.toList());
    }

    private static AuditLogResponse.AuditLogResponseBuilder logResponseBuilder(
            AuditLog entity, AuditSnapshotAssemblerRegistry registry) {
        return AuditLogResponse.builder()
                .id(AuditLogIdCodec.toStringValue(entity.getId()))
                .objectType(entity.getObjectType())
                .objectTypeLabel(objectTypeLabel(entity.getObjectType(), registry))
                .objectId(entity.getObjectId())
                .objectDisplayName(displayName(entity))
                .version(entity.getVersion())
                .action(entity.getAction() == null ? null : entity.getAction().value())
                .actionLabel(actionLabel(entity.getAction()))
                .operatorType(
                        entity.getOperatorType() == null
                                ? null
                                : entity.getOperatorType().value())
                .operatorTypeLabel(operatorTypeLabel(entity.getOperatorType()))
                .operatorId(entity.getOperatorId())
                .operatorName(entity.getOperatorName())
                .source(entity.getSource())
                .requestId(entity.getRequestId())
                .traceId(entity.getTraceId())
                .remoteAddr(entity.getRemoteAddr())
                .summary(entity.getSummary())
                .occurredAt(entity.getOccurredAt())
                .changedFields(toChangedFieldResponses(entity.getChangedFields()));
    }

    private static AuditLogDetailResponse logDetailResponse(AuditLog entity, AuditSnapshotAssemblerRegistry registry) {
        return AuditLogDetailResponse.builder()
                .id(AuditLogIdCodec.toStringValue(entity.getId()))
                .objectType(entity.getObjectType())
                .objectTypeLabel(objectTypeLabel(entity.getObjectType(), registry))
                .objectId(entity.getObjectId())
                .objectDisplayName(displayName(entity))
                .version(entity.getVersion())
                .action(entity.getAction() == null ? null : entity.getAction().value())
                .actionLabel(actionLabel(entity.getAction()))
                .operatorType(
                        entity.getOperatorType() == null
                                ? null
                                : entity.getOperatorType().value())
                .operatorTypeLabel(operatorTypeLabel(entity.getOperatorType()))
                .operatorId(entity.getOperatorId())
                .operatorName(entity.getOperatorName())
                .source(entity.getSource())
                .requestId(entity.getRequestId())
                .traceId(entity.getTraceId())
                .remoteAddr(entity.getRemoteAddr())
                .summary(entity.getSummary())
                .occurredAt(entity.getOccurredAt())
                .changedFields(toChangedFieldResponses(entity.getChangedFields()))
                .idempotencyKey(entity.getIdempotencyKey())
                .previousVersion(entity.getPreviousVersion())
                .beforeSnapshot(toSnapshotResponse(entity.getBeforeSnapshot()))
                .afterSnapshot(toSnapshotResponse(entity.getAfterSnapshot()))
                .build();
    }

    private static List<AuditLogResponse> toLatestLogResponses(
            PageResult<AuditLog> latestLogs, AuditSnapshotAssemblerRegistry registry) {
        if (latestLogs.getRecords() == null) {
            return new ArrayList<>();
        }
        return latestLogs.getRecords().stream()
                .map(log -> logResponseBuilder(log, registry).build())
                .collect(Collectors.toList());
    }

    private static AuditSnapshotResponse toSnapshotResponse(AuditSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return AuditSnapshotResponse.builder()
                .objectType(snapshot.getObjectType())
                .objectId(snapshot.getObjectId())
                .displayName(snapshot.getDisplayName())
                .fields(toSnapshotFieldResponses(snapshot.getFields()))
                .build();
    }

    private static List<AuditSnapshotFieldResponse> toSnapshotFieldResponses(List<AuditField> fields) {
        List<AuditSnapshotFieldResponse> responses = new ArrayList<>();
        if (fields == null) {
            return responses;
        }
        for (AuditField field : fields) {
            responses.add(AuditSnapshotFieldResponse.builder()
                    .fieldName(field.getFieldName())
                    .fieldLabel(field.getFieldLabel())
                    .value(field.getValue())
                    .displayValue(field.getDisplayValue())
                    .valueType(field.getValueType())
                    .sensitive(field.isSensitive())
                    .build());
        }
        return responses;
    }

    private static List<AuditFieldResponse> toChangedFieldResponses(List<AuditChangedField> fields) {
        List<AuditFieldResponse> responses = new ArrayList<>();
        if (fields == null) {
            return responses;
        }
        for (AuditChangedField field : fields) {
            responses.add(AuditFieldResponse.builder()
                    .fieldName(field.getFieldName())
                    .fieldLabel(field.getFieldLabel())
                    .beforeDisplayValue(field.getBeforeDisplayValue())
                    .afterDisplayValue(field.getAfterDisplayValue())
                    .build());
        }
        return responses;
    }

    private static String displayName(AuditLog entity) {
        if (entity.getAfterSnapshot() != null && entity.getAfterSnapshot().getDisplayName() != null) {
            return entity.getAfterSnapshot().getDisplayName();
        }
        if (entity.getBeforeSnapshot() != null) {
            return entity.getBeforeSnapshot().getDisplayName();
        }
        return null;
    }

    private static String objectTypeLabel(String objectType, AuditSnapshotAssemblerRegistry registry) {
        AuditSnapshotAssembler assembler = registry == null ? null : registry.get(objectType);
        return assembler == null ? null : assembler.objectTypeLabel();
    }

    private static String actionLabel(AuditAction action) {
        if (action == null) {
            return null;
        }
        switch (action) {
            case CREATE:
                return "创建";
            case UPDATE:
                return "更新";
            case DELETE:
                return "删除";
            case ENABLE:
                return "启用";
            case DISABLE:
                return "禁用";
            case ARCHIVE:
                return "归档";
            case RESTORE:
                return "恢复";
            case BIND:
                return "绑定";
            case UNBIND:
                return "解绑";
            case UPDATE_RELATION:
                return "更新关系";
            case RESET_CREDENTIAL:
                return "重置凭据";
            default:
                return action.value();
        }
    }

    private static String operatorTypeLabel(AuditOperatorType operatorType) {
        if (operatorType == null) {
            return null;
        }
        switch (operatorType) {
            case USER:
                return "后台用户";
            case MEMBER:
                return "会员";
            case SYSTEM:
                return "系统";
            case UNKNOWN:
                return "未知";
            default:
                return operatorType.value();
        }
    }

    private static AuditOptionResponse option(String value, String label) {
        return AuditOptionResponse.builder().value(value).label(label).build();
    }

    private static AuditObjectFieldResponse objectField(String fieldName, String fieldLabel) {
        return AuditObjectFieldResponse.builder()
                .fieldName(fieldName)
                .fieldLabel(fieldLabel)
                .build();
    }
}
