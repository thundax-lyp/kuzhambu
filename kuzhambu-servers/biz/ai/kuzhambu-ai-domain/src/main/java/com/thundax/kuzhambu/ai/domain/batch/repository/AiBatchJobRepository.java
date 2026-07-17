package com.thundax.kuzhambu.ai.domain.batch.repository;

import com.thundax.kuzhambu.ai.domain.batch.model.entity.AiBatchJob;
import com.thundax.kuzhambu.ai.domain.split.model.entity.EntrySplitCandidate;
import com.thundax.kuzhambu.ai.domain.vision.model.entity.ImageUnderstandingResult;
import java.util.List;

public interface AiBatchJobRepository {

    AiBatchJob get(Long batchId);

    Long insert(AiBatchJob batchJob);

    int update(AiBatchJob batchJob);

    Long insertImageUnderstanding(ImageUnderstandingResult result);

    ImageUnderstandingResult getImageUnderstanding(Long storageObjectId, String contentHash);

    Long insertEntrySplitCandidate(EntrySplitCandidate candidate);

    List<EntrySplitCandidate> listEntrySplitCandidates(Long candidateId);
}
