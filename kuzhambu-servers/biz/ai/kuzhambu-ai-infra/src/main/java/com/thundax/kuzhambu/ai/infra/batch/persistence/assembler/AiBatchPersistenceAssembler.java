package com.thundax.kuzhambu.ai.infra.batch.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.split.model.entity.EntrySplitCandidate;
import com.thundax.kuzhambu.ai.domain.vision.model.entity.ImageUnderstandingResult;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.AiBatchJobDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.EntrySplitCandidateDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.ImageUnderstandingResultDO;
import java.util.ArrayList;
import java.util.List;

public final class AiBatchPersistenceAssembler {

    private AiBatchPersistenceAssembler() {}

    public static AiBatchJobDO toObject(AiBatchJob job) {
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

    public static AiBatchJob toBatchDomain(AiBatchJobDO dataObject) {
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

    public static ImageUnderstandingResultDO toObject(ImageUnderstandingResult result) {
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

    public static ImageUnderstandingResult toImageDomain(ImageUnderstandingResultDO dataObject) {
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

    public static EntrySplitCandidateDO toObject(EntrySplitCandidate candidate) {
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

    public static EntrySplitCandidate toSplitDomain(EntrySplitCandidateDO dataObject) {
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

    public static List<EntrySplitCandidate> toSplitDomainList(List<EntrySplitCandidateDO> dataObjects) {
        List<EntrySplitCandidate> candidates = new ArrayList<>();
        if (dataObjects == null) {
            return candidates;
        }
        for (EntrySplitCandidateDO dataObject : dataObjects) {
            candidates.add(toSplitDomain(dataObject));
        }
        return candidates;
    }
}
