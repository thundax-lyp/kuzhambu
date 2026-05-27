package com.thundax.kuzhambu.system.interfaces.admin.audit.assembler;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.audit.entity.AuditLog;
import com.thundax.kuzhambu.system.application.audit.entity.AuditMeta;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditAction;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditField;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditLogIdCodec;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditMetaIdCodec;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshotAssembler;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditSnapshotAssemblerRegistry;
import com.thundax.kuzhambu.system.application.audit.service.query.AuditLogQuery;
import com.thundax.kuzhambu.system.application.audit.service.query.AuditMetaQuery;
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
import java.util.stream.Collectors;

public final class AuditInterfaceAssembler {

    private AuditInterfaceAssembler() {}

    public static AuditMetaQuery toMetaQuery(AuditMetaRequest request) {
        AuditMetaQuery query = new AuditMetaQuery();
        query.setObjectType(request.getObjectType());
        query.setObjectId(request.getObjectId());
        return query;
    }

    public static AuditLogQuery toLogQuery(AuditLogPageRequest request) {
        AuditLogQuery query = new AuditLogQuery();
        query.setObjectType(request.getObjectType());
        query.setObjectId(request.getObjectId());
        query.setAction(request.getAction() == null ? null : AuditAction.from(request.getAction()));
        query.setOperatorType(
                request.getOperatorType() == null ? null : AuditOperatorType.from(request.getOperatorType()));
        query.setOperatorId(request.getOperatorId());
        query.setSource(request.getSource());
        query.setRequestId(request.getRequestId());
        query.setBeginDate(request.getBeginDate());
        query.setEndDate(request.getEndDate());
        return query;
    }

    public static AuditLogId toLogId(AuditLogDetailRequest request) {
        return AuditLogIdCodec.toDomain(request.getId());
    }

    public static AuditLogQuery toLogQuery(AuditObjectPageRequest request) {
        AuditLogQuery query = new AuditLogQuery();
        query.setObjectType(request.getObjectType());
        query.setObjectId(request.getObjectId());
        return query;
    }

    public static AuditLogQuery toObjectLogQuery(AuditMetaRequest request) {
        AuditLogQuery query = new AuditLogQuery();
        query.setObjectType(request.getObjectType());
        query.setObjectId(request.getObjectId());
        return query;
    }

    public static AuditMetaResponse toMetaResponse(AuditMeta entity) {
        if (entity == null) {
            return AuditMetaResponse.builder().build();
        }
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

    public static AuditLogResponse toLogResponse(AuditLog entity) {
        return toLogResponse(entity, null);
    }

    public static AuditLogResponse toLogResponse(AuditLog entity, AuditSnapshotAssemblerRegistry registry) {
        if (entity == null) {
            return AuditLogResponse.builder().changedFields(new ArrayList<>()).build();
        }
        return logResponseBuilder(entity, registry).build();
    }

    public static AuditLogDetailResponse toLogDetailResponse(AuditLog entity) {
        return toLogDetailResponse(entity, null);
    }

    public static AuditLogDetailResponse toLogDetailResponse(AuditLog entity, AuditSnapshotAssemblerRegistry registry) {
        if (entity == null) {
            return AuditLogDetailResponse.builder()
                    .changedFields(new ArrayList<>())
                    .build();
        }
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

    public static AuditObjectOverviewResponse toOverviewResponse(AuditMeta meta, PageResult<AuditLog> latestLogs) {
        return toOverviewResponse(meta, latestLogs, null);
    }

    public static AuditObjectOverviewResponse toOverviewResponse(
            AuditMeta meta, PageResult<AuditLog> latestLogs, AuditSnapshotAssemblerRegistry registry) {
        return AuditObjectOverviewResponse.builder()
                .meta(toMetaResponse(meta))
                .latestLogs(
                        latestLogs == null || latestLogs.getRecords() == null
                                ? null
                                : latestLogs.getRecords().stream()
                                        .map(log -> AuditInterfaceAssembler.toLogResponse(log, registry))
                                        .collect(Collectors.toList()))
                .build();
    }

    public static AuditOptionsResponse toOptionsResponse(AuditSnapshotAssemblerRegistry registry) {
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

    public static List<AuditObjectFieldResponse> toFieldResponses(
            AuditSnapshotAssemblerRegistry registry, String objectType) {
        AuditSnapshotAssembler assembler = registry == null ? null : registry.get(objectType);
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
