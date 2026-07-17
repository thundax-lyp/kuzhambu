package com.thundax.kuzhambu.ai.infra.batch.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.vision.model.entity.ImageUnderstandingResult;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.ImageUnderstandingResultDO;

public final class ImageUnderstandingResultPersistenceAssembler {

    private ImageUnderstandingResultPersistenceAssembler() {}

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

    public static ImageUnderstandingResult toDomain(ImageUnderstandingResultDO dataObject) {
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
}
