package com.thundax.kuzhambu.ai.domain.batch.repository;

import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.split.model.entity.EntrySplitCandidate;
import com.thundax.kuzhambu.ai.domain.vision.model.entity.ImageUnderstandingResult;
import java.util.List;

public interface AiBatchJobRepository {

    AiBatchJob getBatchJob(Long batchId);

    Long saveBatchJob(AiBatchJob batchJob);

    int updateBatchJob(AiBatchJob batchJob);

    Long saveImageUnderstanding(ImageUnderstandingResult result);

    ImageUnderstandingResult getImageUnderstanding(Long storageObjectId, String contentHash);

    Long saveEntrySplitCandidate(EntrySplitCandidate candidate);

    List<EntrySplitCandidate> listEntrySplitCandidates(Long candidateId);
}
