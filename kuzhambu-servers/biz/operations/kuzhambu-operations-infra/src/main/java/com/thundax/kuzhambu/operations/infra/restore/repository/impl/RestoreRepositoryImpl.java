package com.thundax.kuzhambu.operations.infra.restore.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.restore.codec.RestoreIdCodec;
import com.thundax.kuzhambu.operations.domain.restore.model.entity.RestoreRecord;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import com.thundax.kuzhambu.operations.domain.restore.repository.RestoreRepository;
import com.thundax.kuzhambu.operations.infra.restore.persistence.assembler.RestorePersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.restore.persistence.dataobject.RestoreDO;
import com.thundax.kuzhambu.operations.infra.restore.persistence.mapper.RestoreMapper;
import org.apache.commons.lang3.StringUtils;
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
    public PageResult<RestoreRecord> page(
            Long backupId, String restoreStatus, Long requesterUserId, int pageNo, int pageSize) {
        Page<RestoreDO> page = new Page<>(pageNo, pageSize);
        IPage<RestoreDO> dataObjectPage =
                mapper.selectPage(page, buildPageWrapper(backupId, restoreStatus, requesterUserId));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                RestorePersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
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

    private QueryWrapper<RestoreDO> buildPageWrapper(Long backupId, String restoreStatus, Long requesterUserId) {
        QueryWrapper<RestoreDO> wrapper = new QueryWrapper<>();
        if (backupId != null) {
            wrapper.eq("backup_id", backupId);
        }
        if (StringUtils.isNotBlank(restoreStatus)) {
            wrapper.eq("restore_status", restoreStatus);
        }
        if (requesterUserId != null) {
            wrapper.eq("requester_user_id", requesterUserId);
        }
        wrapper.orderByDesc("started_at");
        return wrapper;
    }
}
