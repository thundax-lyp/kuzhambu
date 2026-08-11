package com.thundax.kuzhambu.discovery.application.search.command;

import java.time.Instant;
import java.util.List;

public record SearchPublicationPrepareCommand(
        String sourceId,
        String contentType,
        String contentId,
        String contentVersionId,
        Integer contentVersionNo,
        String title,
        String summary,
        String categoryId,
        String categoryName,
        String volumeId,
        String volumeTitle,
        List<String> textSegments,
        List<String> tagNames,
        Instant contentUpdatedAt) {}
