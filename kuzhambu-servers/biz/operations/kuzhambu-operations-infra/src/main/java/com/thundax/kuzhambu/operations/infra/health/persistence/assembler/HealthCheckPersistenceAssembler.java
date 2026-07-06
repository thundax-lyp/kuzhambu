package com.thundax.kuzhambu.operations.infra.health.persistence.assembler;

import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthCheckDO;
import java.util.ArrayList;
import java.util.List;

public final class HealthCheckPersistenceAssembler {

    private HealthCheckPersistenceAssembler() {}

    public static HealthCheckDO toObject(HealthCheckRecord entity) {
        return entity == null
                ? null
                : new HealthCheckDO(
                        null,
                        HealthCheckIdCodec.toValue(entity.getId()),
                        entity.getComponent(),
                        entity.getHealthStatus(),
                        entity.getLatencyMs(),
                        entity.getMessage(),
                        entity.getProbeSource(),
                        entity.getProbeTarget(),
                        entity.getDetailsJson(),
                        entity.getCheckedAt());
    }

    public static HealthCheckRecord toDomain(HealthCheckDO dataObject) {
        return dataObject == null
                ? null
                : new HealthCheckRecord(
                        HealthCheckIdCodec.toDomain(dataObject.getCheckId()),
                        dataObject.getComponent(),
                        dataObject.getHealthStatus(),
                        dataObject.getLatencyMs(),
                        dataObject.getMessage(),
                        dataObject.getProbeSource(),
                        dataObject.getProbeTarget(),
                        dataObject.getDetailsJson(),
                        dataObject.getCheckedAt());
    }

    public static List<HealthCheckRecord> toDomainList(List<HealthCheckDO> dataObjects) {
        List<HealthCheckRecord> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toDomain(item)));
        }
        return entities;
    }
}
