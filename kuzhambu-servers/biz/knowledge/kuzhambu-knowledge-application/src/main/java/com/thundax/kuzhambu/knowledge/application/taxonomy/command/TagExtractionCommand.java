package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

public record TagExtractionCommand(
        String sourceContentType,
        Long sourceContentId,
        String contentTitle,
        String contentText,
        Long modelId,
        String modelName,
        Long promptVersionId,
        Integer maxTags,
        Boolean allowNewTags,
        Long requestedBy) {}
