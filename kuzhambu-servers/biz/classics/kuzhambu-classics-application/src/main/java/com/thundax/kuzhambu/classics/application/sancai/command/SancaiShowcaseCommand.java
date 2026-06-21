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
    private StorageObjectId storageObjectId;
    private int entryCount;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;

    public SancaiShowcase toEntity() {
        SancaiShowcase showcase = new SancaiShowcase();
        showcase.setRequestedAt(requestedAt == null ? new Date() : requestedAt);
        showcase.setStatus(status == null ? SancaiShowcaseStatus.REQUESTED : status);
        showcase.setScopeJson(scopeJson);
        showcase.setStorageObjectId(storageObjectId);
        showcase.setEntryCount(entryCount);
        showcase.setVisibilityRiskStatus(visibilityRiskStatus);
        return showcase;
    }
}
