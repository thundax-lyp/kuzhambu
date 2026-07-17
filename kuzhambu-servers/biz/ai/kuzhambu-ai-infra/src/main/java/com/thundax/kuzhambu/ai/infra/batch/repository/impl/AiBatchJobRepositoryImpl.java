package com.thundax.kuzhambu.ai.infra.batch.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.batch.repository.AiBatchJobRepository;
import com.thundax.kuzhambu.ai.domain.split.model.entity.EntrySplitCandidate;
import com.thundax.kuzhambu.ai.domain.vision.model.entity.ImageUnderstandingResult;
import com.thundax.kuzhambu.ai.infra.batch.persistence.assembler.AiBatchJobPersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.batch.persistence.assembler.EntrySplitCandidatePersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.batch.persistence.assembler.ImageUnderstandingResultPersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.AiBatchJobDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.EntrySplitCandidateDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.ImageUnderstandingResultDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.mapper.AiBatchJobMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.time.Instant;
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
    public AiBatchJob get(Long batchId) {
        return AiBatchJobPersistenceAssembler.toDomain(aiBatchJobMapper.selectOne(
                new LambdaQueryWrapper<AiBatchJobDO>().eq(AiBatchJobDO::getBatchId, batchId)));
    }

    @Override
    public Long insert(AiBatchJob batchJob) {
        AiBatchJobDO dataObject = AiBatchJobPersistenceAssembler.toObject(batchJob);
        if (dataObject.getBatchId() == null) {
            dataObject.setBatchId(nextId());
        }
        if (dataObject.getRequestedAt() == null) {
            dataObject.setRequestedAt(Instant.now());
        }
        aiBatchJobMapper.insert(dataObject);
        return dataObject.getBatchId();
    }

    @Override
    public int update(AiBatchJob batchJob) {
        AiBatchJobDO dataObject = AiBatchJobPersistenceAssembler.toObject(batchJob);
        return aiBatchJobMapper.update(
                null,
                new LambdaUpdateWrapper<AiBatchJobDO>()
                        .eq(AiBatchJobDO::getBatchId, dataObject.getBatchId())
                        .set(AiBatchJobDO::getStatus, dataObject.getStatus())
                        .set(AiBatchJobDO::getSuccessCount, dataObject.getSuccessCount())
                        .set(AiBatchJobDO::getFailedCount, dataObject.getFailedCount())
                        .set(AiBatchJobDO::getCancelledCount, dataObject.getCancelledCount())
                        .set(AiBatchJobDO::getFailureSummaryJson, dataObject.getFailureSummaryJson())
                        .set(AiBatchJobDO::getCancelledAt, dataObject.getCancelledAt())
                        .set(AiBatchJobDO::getCompletedAt, dataObject.getCompletedAt()));
    }

    @Override
    public Long insertImageUnderstanding(ImageUnderstandingResult result) {
        ImageUnderstandingResultDO dataObject = ImageUnderstandingResultPersistenceAssembler.toObject(result);
        if (dataObject.getUnderstandingId() == null) {
            dataObject.setUnderstandingId(nextId());
        }
        if (dataObject.getRequestedAt() == null) {
            dataObject.setRequestedAt(Instant.now());
        }
        aiBatchJobMapper.insertImageUnderstanding(dataObject);
        return dataObject.getUnderstandingId();
    }

    @Override
    public ImageUnderstandingResult getImageUnderstanding(Long storageObjectId, String contentHash) {
        return ImageUnderstandingResultPersistenceAssembler.toDomain(
                aiBatchJobMapper.selectImageUnderstanding(storageObjectId, contentHash));
    }

    @Override
    public Long insertEntrySplitCandidate(EntrySplitCandidate candidate) {
        EntrySplitCandidateDO dataObject = EntrySplitCandidatePersistenceAssembler.toObject(candidate);
        if (dataObject.getSplitCandidateId() == null) {
            dataObject.setSplitCandidateId(nextId());
        }
        aiBatchJobMapper.insertEntrySplitCandidate(dataObject);
        return dataObject.getSplitCandidateId();
    }

    @Override
    public List<EntrySplitCandidate> listEntrySplitCandidates(Long candidateId) {
        return EntrySplitCandidatePersistenceAssembler.toDomainList(
                aiBatchJobMapper.selectEntrySplitCandidates(candidateId));
    }

    private Long nextId() {
        return idGenerator.nextId().value();
    }
}
