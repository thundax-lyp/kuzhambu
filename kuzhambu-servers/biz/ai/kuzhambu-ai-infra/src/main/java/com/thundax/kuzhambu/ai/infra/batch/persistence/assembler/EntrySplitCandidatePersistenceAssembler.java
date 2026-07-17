package com.thundax.kuzhambu.ai.infra.batch.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.split.model.entity.EntrySplitCandidate;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.EntrySplitCandidateDO;
import java.util.ArrayList;
import java.util.List;

public final class EntrySplitCandidatePersistenceAssembler {

    private EntrySplitCandidatePersistenceAssembler() {}

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

    public static EntrySplitCandidate toDomain(EntrySplitCandidateDO dataObject) {
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

    public static List<EntrySplitCandidate> toDomainList(List<EntrySplitCandidateDO> dataObjects) {
        List<EntrySplitCandidate> candidates = new ArrayList<>();
        if (dataObjects == null) {
            return candidates;
        }
        for (EntrySplitCandidateDO dataObject : dataObjects) {
            candidates.add(toDomain(dataObject));
        }
        return candidates;
    }
}
