package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import java.util.Date;
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
    private Date requestedAt;
    private SancaiShowcaseStatus status;
    private String scopeJson;
    private StorageObjectId storageObjectId;
    private int entryCount;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
}
