package com.thundax.kuzhambu.knowledge.application.graph.query;

public record KnowledgeRelationQuery(
        Long versionId, String keyword, String relationType, String confirmationStatus, Long relationId) {}
