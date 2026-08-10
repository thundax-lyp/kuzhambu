package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import java.io.InputStream;

public record SancaiEntryImageUploadCommand(
        Long entryId,
        InputStream inputStream,
        String originalFilename,
        String contentType,
        long size,
        String title,
        SancaiEntryImageType imageType,
        boolean currentUsed,
        Long replaceImageId) {}
