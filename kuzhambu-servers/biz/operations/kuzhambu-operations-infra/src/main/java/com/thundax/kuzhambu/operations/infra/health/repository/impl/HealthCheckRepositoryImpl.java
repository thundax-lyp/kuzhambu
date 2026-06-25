package com.thundax.kuzhambu.operations.infra.health.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import com.thundax.kuzhambu.operations.infra.health.persistence.assembler.HealthCheckPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthCheckDO;
import com.thundax.kuzhambu.operations.infra.health.persistence.mapper.HealthCheckMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HealthCheckRepositoryImpl implements HealthCheckRepository {

    private final HealthCheckMapper mapper;

    public HealthCheckRepositoryImpl(HealthCheckMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public HealthCheckRecord getById(HealthCheckId id) {
        return HealthCheckPersistenceAssembler.toDomain(mapper.selectOne(
                new LambdaQueryWrapper<HealthCheckDO>().eq(HealthCheckDO::getCheckId, HealthCheckIdCodec.toValue(id))));
    }

    @Override
    public HealthCheckId insert(HealthCheckRecord record) {
        HealthCheckDO dataObject = HealthCheckPersistenceAssembler.toObject(record);
        mapper.insert(dataObject);
        return HealthCheckIdCodec.toDomain(dataObject.getCheckId());
    }

    @Override
    public int update(HealthCheckRecord record) {
        HealthCheckDO dataObject = HealthCheckPersistenceAssembler.toObject(record);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<HealthCheckDO>()
                        .eq(HealthCheckDO::getCheckId, dataObject.getCheckId())
                        .set(HealthCheckDO::getComponent, dataObject.getComponent())
                        .set(HealthCheckDO::getHealthStatus, dataObject.getHealthStatus())
                        .set(HealthCheckDO::getLatencyMs, dataObject.getLatencyMs())
                        .set(HealthCheckDO::getMessage, dataObject.getMessage())
                        .set(HealthCheckDO::getCheckedAt, dataObject.getCheckedAt()));
    }

    @Override
    public int deleteById(HealthCheckId id) {
        return mapper.delete(
                new LambdaQueryWrapper<HealthCheckDO>().eq(HealthCheckDO::getCheckId, HealthCheckIdCodec.toValue(id)));
    }
}
