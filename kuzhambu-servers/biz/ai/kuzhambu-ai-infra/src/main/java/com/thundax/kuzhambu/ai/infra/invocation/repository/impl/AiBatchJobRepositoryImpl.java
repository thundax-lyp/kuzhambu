package com.thundax.kuzhambu.ai.infra.invocation.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.query.AiBatchJobQuery;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiBatchJobRepository;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.assembler.AiBatchJobPersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiBatchJobDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper.AiBatchJobMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiBatchJobRepositoryImpl implements AiBatchJobRepository {

    private final AiBatchJobMapper aiBatchJobMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AiBatchJobRepositoryImpl(AiBatchJobMapper aiBatchJobMapper) {
        this.aiBatchJobMapper = aiBatchJobMapper;
    }

    @Override
    public AiBatchJob get(AiBatchJobId batchId) {
        return AiBatchJobPersistenceAssembler.toDomain(aiBatchJobMapper.selectOne(
                new LambdaQueryWrapper<AiBatchJobDO>().eq(AiBatchJobDO::getId, AiBatchJobIdCodec.toValue(batchId))));
    }

    @Override
    public AiBatchJobId insert(AiBatchJob batchJob) {
        AiBatchJobDO dataObject = AiBatchJobPersistenceAssembler.toObject(batchJob);
        if (dataObject.getId() == null) {
            dataObject.setId(nextId());
        }
        if (dataObject.getRequestedAt() == null) {
            dataObject.setRequestedAt(Instant.now());
        }
        aiBatchJobMapper.insert(dataObject);
        return AiBatchJobIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(AiBatchJob batchJob) {
        AiBatchJobDO dataObject = AiBatchJobPersistenceAssembler.toObject(batchJob);
        LambdaUpdateWrapper<AiBatchJobDO> updateWrapper =
                new LambdaUpdateWrapper<AiBatchJobDO>().eq(AiBatchJobDO::getId, dataObject.getId());
        fillUpdateWrapper(updateWrapper, dataObject);
        return aiBatchJobMapper.update(null, updateWrapper);
    }

    @Override
    public int updateIfStatus(AiBatchJob batchJob, AiBatchJobStatus expectedStatus) {
        AiBatchJobDO dataObject = AiBatchJobPersistenceAssembler.toObject(batchJob);
        LambdaUpdateWrapper<AiBatchJobDO> updateWrapper = new LambdaUpdateWrapper<AiBatchJobDO>()
                .eq(AiBatchJobDO::getId, dataObject.getId())
                .eq(AiBatchJobDO::getStatus, expectedStatus.name());
        fillUpdateWrapper(updateWrapper, dataObject);
        return aiBatchJobMapper.update(null, updateWrapper);
    }

    @Override
    public List<AiBatchJob> listRunningJobsRequestedBefore(
            String scope, List<AiBusinessCapability> capabilities, Instant requestedBefore, int limit) {
        LambdaQueryWrapper<AiBatchJobDO> queryWrapper = new LambdaQueryWrapper<AiBatchJobDO>()
                .eq(AiBatchJobDO::getStatus, AiBatchJobStatus.RUNNING.name())
                .lt(AiBatchJobDO::getRequestedAt, requestedBefore)
                .orderByAsc(AiBatchJobDO::getRequestedAt)
                .last("limit " + Math.max(1, limit));
        if (!isBlank(scope)) {
            queryWrapper.eq(AiBatchJobDO::getScope, scope);
        }
        if (capabilities != null && !capabilities.isEmpty()) {
            List<String> capabilityValues = new ArrayList<>();
            for (AiBusinessCapability capability : capabilities) {
                if (capability != null) {
                    capabilityValues.add(capability.value());
                }
            }
            if (!capabilityValues.isEmpty()) {
                queryWrapper.in(AiBatchJobDO::getCapability, capabilityValues);
            }
        }
        return toBatchJobs(aiBatchJobMapper.selectList(queryWrapper));
    }

    private void fillUpdateWrapper(LambdaUpdateWrapper<AiBatchJobDO> updateWrapper, AiBatchJobDO dataObject) {
        updateWrapper
                .set(AiBatchJobDO::getStatus, dataObject.getStatus())
                .set(AiBatchJobDO::getSuccessCount, dataObject.getSuccessCount())
                .set(AiBatchJobDO::getFailedCount, dataObject.getFailedCount())
                .set(AiBatchJobDO::getCancelledCount, dataObject.getCancelledCount())
                .set(AiBatchJobDO::getFailureSummaryJson, dataObject.getFailureSummaryJson())
                .set(AiBatchJobDO::getCancelledAt, dataObject.getCancelledAt())
                .set(AiBatchJobDO::getCompletedAt, dataObject.getCompletedAt());
    }

    @Override
    public List<AiBatchJob> listJobs(AiBatchJobQuery query) {
        if (query != null && query.getContentId() != null) {
            return toBatchJobs(aiBatchJobMapper.selectJobsByInvocationContent(
                    query.getScope(),
                    query.getCapability() == null ? null : query.getCapability().value(),
                    capabilityValues(query.getCapabilities()),
                    query.getStatus() == null ? null : query.getStatus().name(),
                    query.getContentType(),
                    query.getContentId(),
                    offset(query),
                    pageSize(query)));
        }
        LambdaQueryWrapper<AiBatchJobDO> queryWrapper =
                batchJobQuery(query).orderByDesc(AiBatchJobDO::getRequestedAt).last(limitClause(query));
        return toBatchJobs(aiBatchJobMapper.selectList(queryWrapper));
    }

    private List<AiBatchJob> toBatchJobs(List<AiBatchJobDO> dataObjects) {
        List<AiBatchJob> jobs = new ArrayList<>();
        if (dataObjects != null) {
            for (AiBatchJobDO dataObject : dataObjects) {
                jobs.add(AiBatchJobPersistenceAssembler.toDomain(dataObject));
            }
        }
        return jobs;
    }

    @Override
    public long countJobs(AiBatchJobQuery query) {
        if (query != null && query.getContentId() != null) {
            return aiBatchJobMapper.countJobsByInvocationContent(
                    query.getScope(),
                    query.getCapability() == null ? null : query.getCapability().value(),
                    capabilityValues(query.getCapabilities()),
                    query.getStatus() == null ? null : query.getStatus().name(),
                    query.getContentType(),
                    query.getContentId());
        }
        return aiBatchJobMapper.selectCount(batchJobQuery(query));
    }

    private LambdaQueryWrapper<AiBatchJobDO> batchJobQuery(AiBatchJobQuery query) {
        LambdaQueryWrapper<AiBatchJobDO> queryWrapper = new LambdaQueryWrapper<>();
        if (query == null) {
            return queryWrapper;
        }
        if (!isBlank(query.getScope())) {
            queryWrapper.eq(AiBatchJobDO::getScope, query.getScope());
        }
        if (query.getCapability() != null) {
            queryWrapper.eq(AiBatchJobDO::getCapability, query.getCapability().value());
        }
        List<String> capabilityValues = capabilityValues(query.getCapabilities());
        if (capabilityValues != null && !capabilityValues.isEmpty()) {
            queryWrapper.in(AiBatchJobDO::getCapability, capabilityValues);
        }
        if (query.getStatus() != null) {
            queryWrapper.eq(AiBatchJobDO::getStatus, query.getStatus().name());
        }
        if (!isBlank(query.getContentType())) {
            queryWrapper.eq(AiBatchJobDO::getContentType, query.getContentType());
        }
        return queryWrapper;
    }

    private String limitClause(AiBatchJobQuery query) {
        return "limit " + offset(query) + ", " + pageSize(query);
    }

    private int offset(AiBatchJobQuery query) {
        int effectivePageNo = query == null || query.getPageNo() <= 0 ? 1 : query.getPageNo();
        return (effectivePageNo - 1) * pageSize(query);
    }

    private int pageSize(AiBatchJobQuery query) {
        return query == null || query.getPageSize() <= 0 ? 10 : query.getPageSize();
    }

    private Long nextId() {
        return idGenerator.nextId().value();
    }

    private List<String> capabilityValues(List<AiBusinessCapability> capabilities) {
        if (capabilities == null || capabilities.isEmpty()) {
            return null;
        }
        List<String> values = new ArrayList<>();
        for (AiBusinessCapability capability : capabilities) {
            if (capability != null) {
                values.add(capability.value());
            }
        }
        return values;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
