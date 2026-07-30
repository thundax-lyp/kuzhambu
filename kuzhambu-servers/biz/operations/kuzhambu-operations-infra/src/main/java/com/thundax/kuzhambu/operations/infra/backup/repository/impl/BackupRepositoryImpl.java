package com.thundax.kuzhambu.operations.infra.backup.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.backup.codec.BackupIdCodec;
import com.thundax.kuzhambu.operations.domain.backup.model.entity.BackupRecord;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupStatus;
import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import com.thundax.kuzhambu.operations.domain.backup.repository.BackupRepository;
import com.thundax.kuzhambu.operations.infra.backup.persistence.assembler.BackupPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.backup.persistence.dataobject.BackupDO;
import com.thundax.kuzhambu.operations.infra.backup.persistence.mapper.BackupMapper;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
    public BackupRecord getByFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        return BackupPersistenceAssembler.toDomain(
                mapper.selectOne(new LambdaQueryWrapper<BackupDO>().eq(BackupDO::getFileName, fileName)));
    }

    @Override
    public PageResult<BackupRecord> page(
            String backupType, String backupStatus, Long requesterUserId, int pageNo, int pageSize) {
        Page<BackupDO> page = new Page<>(pageNo, pageSize);
        IPage<BackupDO> dataObjectPage =
                mapper.selectPage(page, buildPageWrapper(backupType, backupStatus, requesterUserId));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                BackupPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
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

    @Override
    public List<BackupId> listExpiredBackupIds(Instant now, int limit) {
        return mapper
                .selectList(new LambdaQueryWrapper<BackupDO>()
                        .select(BackupDO::getBackupId)
                        .eq(BackupDO::getBackupStatus, BackupStatus.SUCCEEDED.value())
                        .le(BackupDO::getExpiresAt, now)
                        .orderByAsc(BackupDO::getExpiresAt)
                        .last("limit " + limit))
                .stream()
                .map(BackupDO::getBackupId)
                .map(BackupIdCodec::toDomain)
                .toList();
    }

    private QueryWrapper<BackupDO> buildPageWrapper(String backupType, String backupStatus, Long requesterUserId) {
        QueryWrapper<BackupDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(backupType)) {
            wrapper.eq("backup_type", backupType);
        }
        if (StringUtils.isNotBlank(backupStatus)) {
            wrapper.eq("backup_status", backupStatus);
        }
        if (requesterUserId != null) {
            wrapper.eq("requester_user_id", requesterUserId);
        }
        wrapper.orderByDesc("started_at");
        return wrapper;
    }
}
