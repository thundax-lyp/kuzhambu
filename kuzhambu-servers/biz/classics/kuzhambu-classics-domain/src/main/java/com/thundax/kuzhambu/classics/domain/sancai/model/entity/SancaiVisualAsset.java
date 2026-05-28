package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiVisualAsset {
    private SancaiVisualAssetId id;
    private SancaiEntryId entryId;
    private int versionNo;
    private SancaiVisualAssetStatus status;
    private StorageObjectId sourceImageStorageObjectId;
    private StorageObjectId generatedImageStorageObjectId;
    private boolean currentUsed;
    private int textWeight;
    private int imageWeight;
    private String imageAnalysisMarkdown;
    private String fusionDescription;
    private String visualDescription;
    private String generationParamsJson;
}
