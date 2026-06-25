package com.thundax.kuzhambu.operations.infra.task.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotIdCodec;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import com.thundax.kuzhambu.operations.infra.task.persistence.assembler.LongTaskSnapshotPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.task.persistence.dataobject.LongTaskSnapshotDO;
import com.thundax.kuzhambu.operations.infra.task.persistence.mapper.LongTaskSnapshotMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LongTaskSnapshotRepositoryImpl implements LongTaskSnapshotRepository {

    private final LongTaskSnapshotMapper mapper;

    public LongTaskSnapshotRepositoryImpl(LongTaskSnapshotMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public LongTaskSnapshot getById(LongTaskSnapshotId id) {
        return LongTaskSnapshotPersistenceAssembler.toDomain(
                mapper.selectOne(new LambdaQueryWrapper<LongTaskSnapshotDO>()
                        .eq(LongTaskSnapshotDO::getSnapshotId, LongTaskSnapshotIdCodec.toValue(id))));
    }

    @Override
    public LongTaskSnapshotId insert(LongTaskSnapshot snapshot) {
        LongTaskSnapshotDO dataObject = LongTaskSnapshotPersistenceAssembler.toObject(snapshot);
        mapper.insert(dataObject);
        return LongTaskSnapshotIdCodec.toDomain(dataObject.getSnapshotId());
    }

    @Override
    public int update(LongTaskSnapshot snapshot) {
        LongTaskSnapshotDO dataObject = LongTaskSnapshotPersistenceAssembler.toObject(snapshot);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<LongTaskSnapshotDO>()
                        .eq(LongTaskSnapshotDO::getSnapshotId, dataObject.getSnapshotId())
                        .set(LongTaskSnapshotDO::getSourceDomain, dataObject.getSourceDomain())
                        .set(LongTaskSnapshotDO::getTaskType, dataObject.getTaskType())
                        .set(LongTaskSnapshotDO::getTaskKey, dataObject.getTaskKey())
                        .set(LongTaskSnapshotDO::getTaskStatus, dataObject.getTaskStatus())
                        .set(LongTaskSnapshotDO::getTotalCount, dataObject.getTotalCount())
                        .set(LongTaskSnapshotDO::getSuccessCount, dataObject.getSuccessCount())
                        .set(LongTaskSnapshotDO::getFailedCount, dataObject.getFailedCount())
                        .set(LongTaskSnapshotDO::getFailureReason, dataObject.getFailureReason())
                        .set(LongTaskSnapshotDO::getRequestedByUserId, dataObject.getRequestedByUserId())
                        .set(LongTaskSnapshotDO::getStartedAt, dataObject.getStartedAt())
                        .set(LongTaskSnapshotDO::getCompletedAt, dataObject.getCompletedAt())
                        .set(LongTaskSnapshotDO::getSnapshotAt, dataObject.getSnapshotAt()));
    }

    @Override
    public int deleteById(LongTaskSnapshotId id) {
        return mapper.delete(new LambdaQueryWrapper<LongTaskSnapshotDO>()
                .eq(LongTaskSnapshotDO::getSnapshotId, LongTaskSnapshotIdCodec.toValue(id)));
    }
}
