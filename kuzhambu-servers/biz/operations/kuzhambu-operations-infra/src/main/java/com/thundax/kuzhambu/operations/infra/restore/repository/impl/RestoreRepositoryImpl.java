package com.thundax.kuzhambu.operations.infra.restore.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.operations.domain.restore.codec.RestoreIdCodec;
import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import com.thundax.kuzhambu.operations.domain.restore.repository.RestoreRepository;
import com.thundax.kuzhambu.operations.infra.restore.persistence.assembler.RestorePersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.restore.persistence.dataobject.RestoreDO;
import com.thundax.kuzhambu.operations.infra.restore.persistence.mapper.RestoreMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RestoreRepositoryImpl implements RestoreRepository {

    private final RestoreMapper mapper;

    public RestoreRepositoryImpl(RestoreMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public RestoreRecord getById(RestoreId id) {
        return RestorePersistenceAssembler.toDomain(mapper.selectOne(
                new LambdaQueryWrapper<RestoreDO>().eq(RestoreDO::getRestoreId, RestoreIdCodec.toValue(id))));
    }

    @Override
    public RestoreId insert(RestoreRecord record) {
        RestoreDO dataObject = RestorePersistenceAssembler.toObject(record);
        mapper.insert(dataObject);
        return RestoreIdCodec.toDomain(dataObject.getRestoreId());
    }

    @Override
    public int update(RestoreRecord record) {
        RestoreDO dataObject = RestorePersistenceAssembler.toObject(record);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<RestoreDO>()
                        .eq(RestoreDO::getRestoreId, dataObject.getRestoreId())
                        .set(RestoreDO::getBackupId, dataObject.getBackupId())
                        .set(RestoreDO::getPreRestoreBackupId, dataObject.getPreRestoreBackupId())
                        .set(RestoreDO::getRestoreStatus, dataObject.getRestoreStatus())
                        .set(RestoreDO::getWriteBlockEnabled, dataObject.getWriteBlockEnabled())
                        .set(RestoreDO::getFailureReason, dataObject.getFailureReason())
                        .set(RestoreDO::getRequesterUserId, dataObject.getRequesterUserId())
                        .set(RestoreDO::getStartedAt, dataObject.getStartedAt())
                        .set(RestoreDO::getCompletedAt, dataObject.getCompletedAt()));
    }

    @Override
    public int deleteById(RestoreId id) {
        return mapper.delete(
                new LambdaQueryWrapper<RestoreDO>().eq(RestoreDO::getRestoreId, RestoreIdCodec.toValue(id)));
    }
}
