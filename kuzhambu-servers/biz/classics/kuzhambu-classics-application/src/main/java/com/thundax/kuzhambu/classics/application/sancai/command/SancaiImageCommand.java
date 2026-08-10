package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;

public record SancaiImageCommand(
        Long id,
        Long entryId,
        StorageObjectId storageObjectId,
        SancaiEntryImageType imageType,
        String title,
        boolean currentUsed) {}
