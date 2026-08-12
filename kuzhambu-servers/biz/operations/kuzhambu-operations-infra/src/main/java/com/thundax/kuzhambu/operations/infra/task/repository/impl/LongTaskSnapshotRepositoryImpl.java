package com.thundax.kuzhambu.operations.infra.task.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.task.codec.LongTaskSnapshotIdCodec;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import com.thundax.kuzhambu.operations.infra.task.persistence.assembler.LongTaskSnapshotPersistenceAssembler;
import com.thundax.kuzhambu.operations.infra.task.persistence.dataobject.LongTaskSnapshotDO;
import com.thundax.kuzhambu.operations.infra.task.persistence.mapper.LongTaskSnapshotMapper;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class LongTaskSnapshotRepositoryImpl implements LongTaskSnapshotRepository {

    private final LongTaskSnapshotMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

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
    public PageResult<LongTaskSnapshot> page(
            String sourceDomain, String taskType, String taskStatus, int pageNo, int pageSize) {
        Page<LongTaskSnapshotDO> page = new Page<>(pageNo, pageSize);
        IPage<LongTaskSnapshotDO> dataObjectPage =
                mapper.selectPage(page, buildPageWrapper(sourceDomain, taskType, taskStatus));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                LongTaskSnapshotPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public LongTaskSnapshotId insert(LongTaskSnapshot snapshot) {
        LongTaskSnapshotDO dataObject = LongTaskSnapshotPersistenceAssembler.toObject(snapshot);
        if (dataObject.getSnapshotId() == null) {
            dataObject.setSnapshotId(idGenerator.nextId().value());
        }
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

    @Override
    public List<LongTaskSnapshotId> listExpiredSnapshotIds(Instant snapshotBefore, int limit) {
        return mapper
                .selectObjs(new QueryWrapper<LongTaskSnapshotDO>()
                        .select("snapshot_id")
                        .le(snapshotBefore != null, "snapshot_at", snapshotBefore)
                        .ne("task_status", "RUNNING")
                        .orderByAsc("snapshot_at")
                        .orderByAsc("snapshot_id")
                        .last("LIMIT " + limit))
                .stream()
                .map(LongTaskSnapshotRepositoryImpl::longValue)
                .map(LongTaskSnapshotIdCodec::toDomain)
                .toList();
    }

    private QueryWrapper<LongTaskSnapshotDO> buildPageWrapper(String sourceDomain, String taskType, String taskStatus) {
        QueryWrapper<LongTaskSnapshotDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(sourceDomain)) {
            wrapper.eq("source_domain", sourceDomain);
        }
        if (StringUtils.isNotBlank(taskType)) {
            wrapper.eq("task_type", taskType);
        }
        if (StringUtils.isNotBlank(taskStatus)) {
            wrapper.eq("task_status", taskStatus);
        }
        wrapper.orderByDesc("snapshot_at");
        wrapper.orderByDesc("snapshot_id");
        return wrapper;
    }

    private static Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }
}
