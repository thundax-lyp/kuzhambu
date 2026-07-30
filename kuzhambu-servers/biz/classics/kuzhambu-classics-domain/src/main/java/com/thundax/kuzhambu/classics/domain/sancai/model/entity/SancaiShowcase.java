package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiShowcase {
    private SancaiShowcaseId id;
    private Instant requestedAt;
    private Instant completedAt;
    private SancaiShowcaseStatus status;
    private String scopeJson;
    private String scopeTitle;
    private StorageObjectId storageObjectId;
    private int entryCount;
    private int assetCount;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
    private String filename;
    private String contentType;
    private Long sizeBytes;
    private String sha256;
    private String failureType;
    private String failureMessage;

    public void markCompleted(StorageObjectId storageObjectId, int entryCount) {
        markCompleted(storageObjectId, entryCount, assetCount, filename, contentType, sizeBytes, sha256);
    }

    public void markCompleted(
            StorageObjectId storageObjectId,
            int entryCount,
            int assetCount,
            String filename,
            String contentType,
            Long sizeBytes,
            String sha256) {
        this.status = SancaiShowcaseStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.storageObjectId = storageObjectId;
        this.entryCount = entryCount;
        this.assetCount = assetCount;
        this.filename = filename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
        this.failureType = null;
        this.failureMessage = null;
    }

    public void markFailed() {
        markFailed(null, null);
    }

    public void markFailed(String failureType, String failureMessage) {
        this.status = SancaiShowcaseStatus.FAILED;
        this.completedAt = Instant.now();
        this.failureType = failureType;
        this.failureMessage = failureMessage;
    }

    public void markExpired() {
        this.status = SancaiShowcaseStatus.EXPIRED;
    }
}
