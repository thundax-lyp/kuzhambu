package com.thundax.kuzhambu.ai.domain.vision.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageUnderstandingResult {

    private Long id;
    private Long understandingId;
    private Long storageObjectId;
    private String contentHash;
    private String analysisMarkdown;
    private Long callId;
    private Long promptVersionId;
    private String modelName;
    private Instant requestedAt;

    public boolean matchesImage(Long targetStorageObjectId, String targetContentHash) {
        return storageObjectId != null
                && storageObjectId.equals(targetStorageObjectId)
                && contentHash != null
                && contentHash.equals(targetContentHash);
    }
}
