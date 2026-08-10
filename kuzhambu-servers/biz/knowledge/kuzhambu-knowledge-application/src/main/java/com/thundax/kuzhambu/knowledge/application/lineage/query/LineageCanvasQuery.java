package com.thundax.kuzhambu.knowledge.application.lineage.query;

public record LineageCanvasQuery(
        Long versionId,
        Long focusNodeId,
        Long focusRelationId,
        String keyword,
        String nodeType,
        String relationType,
        String confirmationStatus,
        Integer depth) {}
