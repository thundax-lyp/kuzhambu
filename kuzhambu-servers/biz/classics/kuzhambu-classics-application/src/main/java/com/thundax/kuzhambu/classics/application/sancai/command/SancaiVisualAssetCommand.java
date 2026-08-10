package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;

public record SancaiVisualAssetCommand(
        SancaiVisualAssetId id,
        SancaiEntryId entryId,
        int versionNo,
        SancaiVisualAssetStatus status,
        StorageObjectId sourceImageStorageObjectId,
        StorageObjectId generatedImageStorageObjectId,
        boolean currentUsed,
        Integer textWeight,
        Integer imageWeight,
        String imageAnalysisMarkdown,
        String fusionDescription,
        String visualDescription,
        String generationParamsJson) {}
