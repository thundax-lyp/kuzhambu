package com.thundax.kuzhambu.ai.infra.batch.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.batch.repository.AiBatchJobRepository;
import com.thundax.kuzhambu.ai.domain.split.model.entity.EntrySplitCandidate;
import com.thundax.kuzhambu.ai.domain.vision.model.entity.ImageUnderstandingResult;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.AiBatchJobDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.EntrySplitCandidateDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.ImageUnderstandingResultDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.mapper.AiBatchJobMapper;
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
    public AiBatchJob get(Long batchId) {
        return toBatchDomain(aiBatchJobMapper.selectOne(
                new LambdaQueryWrapper<AiBatchJobDO>().eq(AiBatchJobDO::getBatchId, batchId)));
    }

    @Override
    public Long insert(AiBatchJob batchJob) {
        AiBatchJobDO dataObject = toBatchObject(batchJob);
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
        AiBatchJobDO dataObject = toBatchObject(batchJob);
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
        ImageUnderstandingResultDO dataObject = toImageObject(result);
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
        return toImageDomain(aiBatchJobMapper.selectImageUnderstanding(storageObjectId, contentHash));
    }

    @Override
    public Long insertEntrySplitCandidate(EntrySplitCandidate candidate) {
        EntrySplitCandidateDO dataObject = toSplitObject(candidate);
        if (dataObject.getSplitCandidateId() == null) {
            dataObject.setSplitCandidateId(nextId());
        }
        aiBatchJobMapper.insertEntrySplitCandidate(dataObject);
        return dataObject.getSplitCandidateId();
    }

    @Override
    public List<EntrySplitCandidate> listEntrySplitCandidates(Long candidateId) {
        return toSplitDomainList(aiBatchJobMapper.selectEntrySplitCandidates(candidateId));
    }

    private AiBatchJobDO toBatchObject(AiBatchJob job) {
        if (job == null) {
            return null;
        }
        AiBatchJobDO dataObject = new AiBatchJobDO();
        dataObject.setId(job.getId());
        dataObject.setBatchId(job.getBatchId());
        dataObject.setScope(job.getScope());
        dataObject.setCapability(job.getCapability());
        dataObject.setContentType(job.getContentType());
        dataObject.setStatus(job.getStatus());
        dataObject.setTotalCount(job.getTotalCount());
        dataObject.setSuccessCount(job.getSuccessCount());
        dataObject.setFailedCount(job.getFailedCount());
        dataObject.setCancelledCount(job.getCancelledCount());
        dataObject.setFailureSummaryJson(job.getFailureSummaryJson());
        dataObject.setRequestedAt(job.getRequestedAt());
        dataObject.setCancelledAt(job.getCancelledAt());
        dataObject.setCompletedAt(job.getCompletedAt());
        return dataObject;
    }

    private AiBatchJob toBatchDomain(AiBatchJobDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiBatchJob(
                dataObject.getId(),
                dataObject.getBatchId(),
                dataObject.getScope(),
                dataObject.getCapability(),
                dataObject.getContentType(),
                dataObject.getStatus(),
                dataObject.getTotalCount() == null ? 0 : dataObject.getTotalCount(),
                dataObject.getSuccessCount() == null ? 0 : dataObject.getSuccessCount(),
                dataObject.getFailedCount() == null ? 0 : dataObject.getFailedCount(),
                dataObject.getCancelledCount() == null ? 0 : dataObject.getCancelledCount(),
                dataObject.getFailureSummaryJson(),
                dataObject.getRequestedAt(),
                dataObject.getCancelledAt(),
                dataObject.getCompletedAt());
    }

    private ImageUnderstandingResultDO toImageObject(ImageUnderstandingResult result) {
        if (result == null) {
            return null;
        }
        ImageUnderstandingResultDO dataObject = new ImageUnderstandingResultDO();
        dataObject.setId(result.getId());
        dataObject.setUnderstandingId(result.getUnderstandingId());
        dataObject.setStorageObjectId(result.getStorageObjectId());
        dataObject.setContentHash(result.getContentHash());
        dataObject.setAnalysisMarkdown(result.getAnalysisMarkdown());
        dataObject.setCallId(result.getCallId());
        dataObject.setPromptVersionId(result.getPromptVersionId());
        dataObject.setModelName(result.getModelName());
        dataObject.setRequestedAt(result.getRequestedAt());
        return dataObject;
    }

    private ImageUnderstandingResult toImageDomain(ImageUnderstandingResultDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new ImageUnderstandingResult(
                dataObject.getId(),
                dataObject.getUnderstandingId(),
                dataObject.getStorageObjectId(),
                dataObject.getContentHash(),
                dataObject.getAnalysisMarkdown(),
                dataObject.getCallId(),
                dataObject.getPromptVersionId(),
                dataObject.getModelName(),
                dataObject.getRequestedAt());
    }

    private EntrySplitCandidateDO toSplitObject(EntrySplitCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        EntrySplitCandidateDO dataObject = new EntrySplitCandidateDO();
        dataObject.setId(candidate.getId());
        dataObject.setSplitCandidateId(candidate.getSplitCandidateId());
        dataObject.setCandidateId(candidate.getCandidateId());
        dataObject.setParentContentType(candidate.getParentContentType());
        dataObject.setParentContentId(candidate.getParentContentId());
        dataObject.setTitle(candidate.getTitle());
        dataObject.setOriginalText(candidate.getOriginalText());
        dataObject.setTranslationText(candidate.getTranslationText());
        dataObject.setTargetVolumeId(candidate.getTargetVolumeId());
        dataObject.setPriority(candidate.getPriority());
        return dataObject;
    }

    private EntrySplitCandidate toSplitDomain(EntrySplitCandidateDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new EntrySplitCandidate(
                dataObject.getId(),
                dataObject.getSplitCandidateId(),
                dataObject.getCandidateId(),
                dataObject.getParentContentType(),
                dataObject.getParentContentId(),
                dataObject.getTitle(),
                dataObject.getOriginalText(),
                dataObject.getTranslationText(),
                dataObject.getTargetVolumeId(),
                dataObject.getPriority() == null ? 0 : dataObject.getPriority());
    }

    private List<EntrySplitCandidate> toSplitDomainList(List<EntrySplitCandidateDO> dataObjects) {
        List<EntrySplitCandidate> candidates = new ArrayList<>();
        if (dataObjects == null) {
            return candidates;
        }
        for (EntrySplitCandidateDO dataObject : dataObjects) {
            candidates.add(toSplitDomain(dataObject));
        }
        return candidates;
    }

    private Long nextId() {
        return idGenerator.nextId().value();
    }
}
