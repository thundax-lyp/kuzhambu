package com.thundax.kuzhambu.operations.infra.backup.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import com.thundax.kuzhambu.operations.infra.backup.persistence.assembler.BackupPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.backup.persistence.dataobject.BackupDO;
import com.thundax.kuzhambu.operations.infra.backup.persistence.mapper.BackupMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BackupRepositoryImpl implements BackupRepository {

    private final BackupMapper mapper;

    public BackupRepositoryImpl(BackupMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BackupRecord getById(BackupId id) {
        return BackupPersistenceAssembler.toDomain(mapper.selectOne(
                new LambdaQueryWrapper<BackupDO>().eq(BackupDO::getBackupId, BackupIdCodec.toValue(id))));
    }

    @Override
    public BackupId insert(BackupRecord record) {
        BackupDO dataObject = BackupPersistenceAssembler.toObject(record);
        mapper.insert(dataObject);
        return BackupIdCodec.toDomain(dataObject.getBackupId());
    }

    @Override
    public int update(BackupRecord record) {
        BackupDO dataObject = BackupPersistenceAssembler.toObject(record);
        return mapper.update(
                null,
                new LambdaUpdateWrapper<BackupDO>()
                        .eq(BackupDO::getBackupId, dataObject.getBackupId())
                        .set(BackupDO::getBackupType, dataObject.getBackupType())
                        .set(BackupDO::getBackupStatus, dataObject.getBackupStatus())
                        .set(BackupDO::getStorageObjectId, dataObject.getStorageObjectId())
                        .set(BackupDO::getFileName, dataObject.getFileName())
                        .set(BackupDO::getFileSizeBytes, dataObject.getFileSizeBytes())
                        .set(BackupDO::getChecksum, dataObject.getChecksum())
                        .set(BackupDO::getFailureReason, dataObject.getFailureReason())
                        .set(BackupDO::getRequesterUserId, dataObject.getRequesterUserId())
                        .set(BackupDO::getStartedAt, dataObject.getStartedAt())
                        .set(BackupDO::getCompletedAt, dataObject.getCompletedAt())
                        .set(BackupDO::getExpiresAt, dataObject.getExpiresAt()));
    }

    @Override
    public int deleteById(BackupId id) {
        return mapper.delete(new LambdaQueryWrapper<BackupDO>().eq(BackupDO::getBackupId, BackupIdCodec.toValue(id)));
    }
}
