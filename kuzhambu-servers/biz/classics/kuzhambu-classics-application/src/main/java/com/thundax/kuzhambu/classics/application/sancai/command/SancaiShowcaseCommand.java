package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import java.time.Instant;

public record SancaiShowcaseCommand(
        Instant requestedAt,
        SancaiShowcaseStatus status,
        String scopeJson,
        String scopeTitle,
        StorageObjectId storageObjectId,
        int entryCount,
        SancaiVisibilityRiskStatus visibilityRiskStatus,
        boolean privateConfirmed) {
    public SancaiShowcaseCommand(
            Instant requestedAt,
            SancaiShowcaseStatus status,
            String scopeJson,
            StorageObjectId storageObjectId,
            int entryCount,
            SancaiVisibilityRiskStatus visibilityRiskStatus) {
        this(requestedAt, status, scopeJson, null, storageObjectId, entryCount, visibilityRiskStatus, false);
    }
}
