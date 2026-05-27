package com.thundax.kuzhambu.system.infra.audit.assembler;

import com.thundax.kuzhambu.system.domain.audit.codec.AuditLogIdCodec;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditMetaIdCodec;
import com.thundax.kuzhambu.system.domain.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.infra.audit.dataobject.AuditMetaDO;

public final class AuditMetaPersistenceAssembler {

    private AuditMetaPersistenceAssembler() {}

    public static AuditMetaDO toDataObject(AuditMeta entity) {
        if (entity == null) {
            return null;
        }
        AuditMetaDO dataObject = new AuditMetaDO();
        dataObject.setId(AuditMetaIdCodec.toValue(entity.getId()));
        dataObject.setObjectType(entity.getObjectType());
        dataObject.setObjectId(entity.getObjectId());
        dataObject.setVersion(entity.getVersion());
        dataObject.setLastLogId(AuditLogIdCodec.toValue(entity.getLastLogId()));
        dataObject.setLastAction(
                entity.getLastAction() == null ? null : entity.getLastAction().value());
        dataObject.setLastOperatorType(
                entity.getLastOperatorType() == null
                        ? null
                        : entity.getLastOperatorType().value());
        dataObject.setLastOperatorId(entity.getLastOperatorId());
        dataObject.setLastOperatorName(entity.getLastOperatorName());
        dataObject.setLastOperatedAt(entity.getLastOperatedAt());
        dataObject.setCreatedLogId(AuditLogIdCodec.toValue(entity.getCreatedLogId()));
        dataObject.setCreatedAt(entity.getCreatedAt());
        return dataObject;
    }

    public static AuditMeta toEntity(AuditMetaDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AuditMeta entity = new AuditMeta();
        entity.setId(AuditMetaIdCodec.toDomain(dataObject.getId()));
        entity.setObjectType(dataObject.getObjectType());
        entity.setObjectId(dataObject.getObjectId());
        entity.setVersion(dataObject.getVersion());
        entity.setLastLogId(AuditLogIdCodec.toDomain(dataObject.getLastLogId()));
        entity.setLastAction(AuditAction.from(dataObject.getLastAction()));
        entity.setLastOperatorType(AuditOperatorType.from(dataObject.getLastOperatorType()));
        entity.setLastOperatorId(dataObject.getLastOperatorId());
        entity.setLastOperatorName(dataObject.getLastOperatorName());
        entity.setLastOperatedAt(dataObject.getLastOperatedAt());
        entity.setCreatedLogId(AuditLogIdCodec.toDomain(dataObject.getCreatedLogId()));
        entity.setCreatedAt(dataObject.getCreatedAt());
        return entity;
    }
}
