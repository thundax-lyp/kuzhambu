package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiShowcaseCommand {
    private Instant requestedAt;
    private SancaiShowcaseStatus status;
    private String scopeJson;
    private String scopeTitle;
    private StorageObjectId storageObjectId;
    private int entryCount;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
    private boolean privateConfirmed;

    public SancaiShowcaseCommand(
            Instant requestedAt,
            SancaiShowcaseStatus status,
            String scopeJson,
            StorageObjectId storageObjectId,
            int entryCount,
            SancaiVisibilityRiskStatus visibilityRiskStatus) {
        this.requestedAt = requestedAt;
        this.status = status;
        this.scopeJson = scopeJson;
        this.storageObjectId = storageObjectId;
        this.entryCount = entryCount;
        this.visibilityRiskStatus = visibilityRiskStatus;
    }
}
