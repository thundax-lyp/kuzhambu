package com.thundax.kuzhambu.operations.infra.health.persistence.assembler;

import com.thundax.kuzhambu.operations.domain.health.codec.HealthAlertIdCodec;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthAlertDO;
import java.util.ArrayList;
import java.util.List;

public final class HealthAlertPersistenceAssembler {

    private HealthAlertPersistenceAssembler() {}

    public static HealthAlertDO toObject(HealthAlertRecord entity) {
        return entity == null
                ? null
                : new HealthAlertDO(
                        null,
                        HealthAlertIdCodec.toValue(entity.getId()),
                        entity.getComponent(),
                        entity.getAlertType(),
                        entity.getAlertLevel(),
                        entity.getAlertStatus(),
                        entity.getSourceRefType(),
                        entity.getSourceRefId(),
                        HealthCheckIdCodec.toValue(entity.getLatestCheckId()),
                        entity.getMessage(),
                        entity.getSuggestion(),
                        entity.getRecoveryAction(),
                        entity.getRecoveryTarget(),
                        entity.getFirstTriggeredAt(),
                        entity.getLastTriggeredAt(),
                        entity.getAckedAt(),
                        entity.getAckedByUserId(),
                        entity.getRecoveredAt(),
                        entity.getFailureReason());
    }

    public static HealthAlertRecord toDomain(HealthAlertDO dataObject) {
        return dataObject == null
                ? null
                : new HealthAlertRecord(
                        HealthAlertIdCodec.toDomain(dataObject.getAlertId()),
                        dataObject.getComponent(),
                        dataObject.getAlertType(),
                        dataObject.getAlertLevel(),
                        dataObject.getAlertStatus(),
                        dataObject.getSourceRefType(),
                        dataObject.getSourceRefId(),
                        HealthCheckIdCodec.toDomain(dataObject.getLatestCheckId()),
                        dataObject.getMessage(),
                        dataObject.getSuggestion(),
                        dataObject.getRecoveryAction(),
                        dataObject.getRecoveryTarget(),
                        dataObject.getFirstTriggeredAt(),
                        dataObject.getLastTriggeredAt(),
                        dataObject.getAckedAt(),
                        dataObject.getAckedByUserId(),
                        dataObject.getRecoveredAt(),
                        dataObject.getFailureReason());
    }

    public static List<HealthAlertRecord> toDomainList(List<HealthAlertDO> dataObjects) {
        List<HealthAlertRecord> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toDomain(item)));
        }
        return entities;
    }
}
