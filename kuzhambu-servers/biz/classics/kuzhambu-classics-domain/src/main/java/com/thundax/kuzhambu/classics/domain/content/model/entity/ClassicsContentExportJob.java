package com.thundax.kuzhambu.classics.domain.content.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
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
public class ClassicsContentExportJob {
    private ClassicsContentExportJobId id;
    private ClassicsExportKind exportKind;
    private ClassicsContentType contentType;
    private ClassicsExportFormat exportFormat;
    private ClassicsExportScopeType scopeType;
    private String scopeJson;
    private Date requestedAt;
    private Date expiresAt;
    private ClassicsExportStatus status;
    private StorageObjectId storageObjectId;
    private int itemCount;
    private int assetCount;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
    private boolean contentChanged;
}
