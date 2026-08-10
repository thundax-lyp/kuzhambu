package com.thundax.kuzhambu.knowledge.application.graph.query;

public record KnowledgeLineageNodeQuery(
        Long versionId, String keyword, String nodeType, String confirmationStatus, Long nodeId) {}
