package com.thundax.kuzhambu.system.infra.audit.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditLogIdCodec;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditMetaIdCodec;
import com.thundax.kuzhambu.system.domain.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.domain.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.system.infra.audit.dataobject.AuditLogDO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AuditLogPersistenceAssembler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<AuditChangedField>> CHANGED_FIELD_LIST_TYPE =
            new TypeReference<List<AuditChangedField>>() {};

    private AuditLogPersistenceAssembler() {}

    public static AuditLogDO toDataObject(AuditLog entity) {
        if (entity == null) {
            return null;
        }
        AuditLogDO dataObject = new AuditLogDO();
        dataObject.setId(AuditLogIdCodec.toValue(entity.getId()));
        dataObject.setMetaId(AuditMetaIdCodec.toValue(entity.getMetaId()));
        dataObject.setObjectType(entity.getObjectType());
        dataObject.setObjectId(entity.getObjectId());
        dataObject.setVersion(entity.getVersion());
        dataObject.setPreviousVersion(entity.getPreviousVersion());
        dataObject.setAction(
                entity.getAction() == null ? null : entity.getAction().value());
        dataObject.setIdempotencyKey(entity.getIdempotencyKey());
        dataObject.setOperatorType(
                entity.getOperatorType() == null
                        ? null
                        : entity.getOperatorType().value());
        dataObject.setOperatorId(entity.getOperatorId());
        dataObject.setOperatorName(entity.getOperatorName());
        dataObject.setSource(entity.getSource());
        dataObject.setRequestId(entity.getRequestId());
        dataObject.setTraceId(entity.getTraceId());
        dataObject.setRemoteAddr(entity.getRemoteAddr());
        dataObject.setSummary(entity.getSummary());
        dataObject.setSnapshotSchemaVersion(entity.getSnapshotSchemaVersion());
        dataObject.setBeforeSnapshot(toJson(entity.getBeforeSnapshot()));
        dataObject.setAfterSnapshot(toJson(entity.getAfterSnapshot()));
        dataObject.setChangedFields(toJson(entity.getChangedFields()));
        dataObject.setOccurredAt(entity.getOccurredAt());
        return dataObject;
    }

    public static AuditLog toEntity(AuditLogDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AuditLog entity = new AuditLog();
        entity.setId(AuditLogIdCodec.toDomain(dataObject.getId()));
        entity.setMetaId(AuditMetaIdCodec.toDomain(dataObject.getMetaId()));
        entity.setObjectType(dataObject.getObjectType());
        entity.setObjectId(dataObject.getObjectId());
        entity.setVersion(dataObject.getVersion());
        entity.setPreviousVersion(dataObject.getPreviousVersion());
        entity.setAction(AuditAction.from(dataObject.getAction()));
        entity.setIdempotencyKey(dataObject.getIdempotencyKey());
        entity.setOperatorType(AuditOperatorType.from(dataObject.getOperatorType()));
        entity.setOperatorId(dataObject.getOperatorId());
        entity.setOperatorName(dataObject.getOperatorName());
        entity.setSource(dataObject.getSource());
        entity.setRequestId(dataObject.getRequestId());
        entity.setTraceId(dataObject.getTraceId());
        entity.setRemoteAddr(dataObject.getRemoteAddr());
        entity.setSummary(dataObject.getSummary());
        entity.setSnapshotSchemaVersion(dataObject.getSnapshotSchemaVersion());
        entity.setBeforeSnapshot(fromJson(dataObject.getBeforeSnapshot(), AuditSnapshot.class));
        entity.setAfterSnapshot(fromJson(dataObject.getAfterSnapshot(), AuditSnapshot.class));
        entity.setChangedFields(fromJson(dataObject.getChangedFields(), CHANGED_FIELD_LIST_TYPE));
        entity.setOccurredAt(dataObject.getOccurredAt());
        return entity;
    }

    public static List<AuditLog> toEntityList(List<AuditLogDO> dataObjects) {
        List<AuditLog> entities = new ArrayList<>();
        if (dataObjects == null) {
            return entities;
        }
        for (AuditLogDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize audit log json field", e);
        }
    }

    private static <T> T fromJson(String value, Class<T> type) {
        if (value == null || value.trim().isEmpty() || "null".equals(value.trim())) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize audit log json field", e);
        }
    }

    private static <T> T fromJson(String value, TypeReference<T> type) {
        if (value == null || value.trim().isEmpty() || "null".equals(value.trim())) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize audit log json field", e);
        }
    }
}
