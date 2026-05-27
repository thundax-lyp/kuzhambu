package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiVisualAsset {
    private Long id;
    private Long entryId;
    private int versionNo;
    private SancaiVisualAssetStatus status;
    private Long sourceImageStorageObjectId;
    private Long generatedImageStorageObjectId;
    private boolean currentUsed;
    private int textWeight;
    private int imageWeight;
    private String imageAnalysisMarkdown;
    private String fusionDescription;
    private String visualDescription;
    private String generationParamsJson;
}
