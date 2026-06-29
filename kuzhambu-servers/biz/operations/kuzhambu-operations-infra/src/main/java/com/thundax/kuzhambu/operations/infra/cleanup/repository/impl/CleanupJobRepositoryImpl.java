package com.thundax.kuzhambu.operations.infra.cleanup.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupItem;
import com.thundax.kuzhambu.operations.domain.cleanup.model.entity.CleanupJob;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupItemId;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import com.thundax.kuzhambu.operations.domain.cleanup.repository.CleanupJobRepository;
import com.thundax.kuzhambu.operations.infra.cleanup.persistence.assembler.CleanupPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.cleanup.persistence.dataobject.CleanupItemDO;
import com.thundax.kuzhambu.operations.infra.cleanup.persistence.dataobject.CleanupJobDO;
import com.thundax.kuzhambu.operations.infra.cleanup.persistence.mapper.CleanupItemMapper;
import com.thundax.kuzhambu.operations.infra.cleanup.persistence.mapper.CleanupJobMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class CleanupJobRepositoryImpl implements CleanupJobRepository {

    private final CleanupJobMapper jobMapper;
    private final CleanupItemMapper itemMapper;

    public CleanupJobRepositoryImpl(CleanupJobMapper jobMapper, CleanupItemMapper itemMapper) {
        this.jobMapper = jobMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public CleanupJob getById(CleanupJobId id) {
        CleanupJob job = CleanupPersistenceAssembler.toDomain(
                jobMapper.selectOne(new LambdaQueryWrapper<CleanupJobDO>().eq(CleanupJobDO::getCleanupId, id.value())));
        if (job != null) {
            job.setCleanupItems(listItemsByJobId(id));
        }
        return job;
    }

    @Override
    public CleanupJobId insert(CleanupJob job) {
        CleanupJobDO dataObject = CleanupPersistenceAssembler.toObject(job);
        jobMapper.insert(dataObject);
        return CleanupJobId.of(dataObject.getCleanupId());
    }

    @Override
    public int update(CleanupJob job) {
        CleanupJobDO dataObject = CleanupPersistenceAssembler.toObject(job);
        return jobMapper.update(
                null,
                new LambdaUpdateWrapper<CleanupJobDO>()
                        .eq(CleanupJobDO::getCleanupId, dataObject.getCleanupId())
                        .set(CleanupJobDO::getCleanupType, dataObject.getCleanupType())
                        .set(CleanupJobDO::getCleanupStatus, dataObject.getCleanupStatus())
                        .set(CleanupJobDO::getTotalCount, dataObject.getTotalCount())
                        .set(CleanupJobDO::getSuccessCount, dataObject.getSuccessCount())
                        .set(CleanupJobDO::getFailedCount, dataObject.getFailedCount())
                        .set(CleanupJobDO::getFailureReason, dataObject.getFailureReason())
                        .set(CleanupJobDO::getRequesterUserId, dataObject.getRequesterUserId())
                        .set(CleanupJobDO::getStartedAt, dataObject.getStartedAt())
                        .set(CleanupJobDO::getCompletedAt, dataObject.getCompletedAt()));
    }

    @Override
    public int deleteById(CleanupJobId id) {
        return jobMapper.delete(new LambdaQueryWrapper<CleanupJobDO>().eq(CleanupJobDO::getCleanupId, id.value()));
    }

    @Override
    public List<CleanupItem> listItemsByJobId(CleanupJobId jobId) {
        List<CleanupItemDO> dataObjects = itemMapper.selectList(
                new LambdaQueryWrapper<CleanupItemDO>().eq(CleanupItemDO::getCleanupId, jobId.value()).orderByAsc("id"));
        return CleanupPersistenceAssembler.toDomainList(dataObjects);
    }

    @Override
    public CleanupItemId insertItem(CleanupItem item) {
        CleanupItemDO dataObject = CleanupPersistenceAssembler.toObject(item);
        itemMapper.insert(dataObject);
        return CleanupItemId.of(dataObject.getCleanupItemId());
    }

    @Override
    public int updateItem(CleanupItem item) {
        CleanupItemDO dataObject = CleanupPersistenceAssembler.toObject(item);
        return itemMapper.update(
                null,
                new LambdaUpdateWrapper<CleanupItemDO>()
                        .eq(CleanupItemDO::getCleanupItemId, dataObject.getCleanupItemId())
                        .set(CleanupItemDO::getCleanupId, dataObject.getCleanupId())
                        .set(CleanupItemDO::getTargetType, dataObject.getTargetType())
                        .set(CleanupItemDO::getTargetId, dataObject.getTargetId())
                        .set(CleanupItemDO::getItemStatus, dataObject.getItemStatus())
                        .set(CleanupItemDO::getFailureReason, dataObject.getFailureReason())
                        .set(CleanupItemDO::getProcessedAt, dataObject.getProcessedAt()));
    }

    @Override
    public int deleteItemsByJobId(CleanupJobId jobId) {
        return itemMapper.delete(new LambdaQueryWrapper<CleanupItemDO>().eq(CleanupItemDO::getCleanupId, jobId.value()));
    }

    @Override
    public PageResult<CleanupJob> page(
            String cleanupType, String cleanupStatus, Long requesterUserId, int pageNo, int pageSize) {
        Page<CleanupJobDO> page = new Page<>(pageNo, pageSize);
        IPage<CleanupJobDO> dataObjectPage =
                jobMapper.selectPage(page, buildPageWrapper(cleanupType, cleanupStatus, requesterUserId));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                dataObjectPage.getRecords().stream()
                        .map(CleanupPersistenceAssembler::toDomain)
                        .toList());
    }

    private QueryWrapper<CleanupJobDO> buildPageWrapper(
            String cleanupType, String cleanupStatus, Long requesterUserId) {
        QueryWrapper<CleanupJobDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(cleanupType)) {
            wrapper.eq("cleanup_type", cleanupType);
        }
        if (StringUtils.isNotBlank(cleanupStatus)) {
            wrapper.eq("cleanup_status", cleanupStatus);
        }
        if (requesterUserId != null) {
            wrapper.eq("requester_user_id", requesterUserId);
        }
        wrapper.orderByDesc("started_at");
        return wrapper;
    }
}
