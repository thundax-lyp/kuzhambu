package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiShowcaseCommand {
    private Date requestedAt;
    private SancaiShowcaseStatus status;
    private String scopeJson;
    private String scopeTitle;
    private StorageObjectId storageObjectId;
    private int entryCount;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
    private boolean privateConfirmed;

    public SancaiShowcaseCommand(
            Date requestedAt,
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

    public SancaiShowcase toEntity() {
        SancaiShowcase showcase = new SancaiShowcase();
        showcase.setRequestedAt(requestedAt == null ? new Date() : requestedAt);
        showcase.setStatus(status == null ? SancaiShowcaseStatus.REQUESTED : status);
        showcase.setScopeJson(scopeJson);
        showcase.setScopeTitle(scopeTitle);
        showcase.setEntryCount(entryCount);
        showcase.setVisibilityRiskStatus(visibilityRiskStatus);
        return showcase;
    }
}
