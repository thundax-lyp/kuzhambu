package com.thundax.kuzhambu.knowledge.application.graph.query;

public record KnowledgeLineageRelationQuery(
        Long versionId, String keyword, String relationType, String confirmationStatus, Long relationId) {}
