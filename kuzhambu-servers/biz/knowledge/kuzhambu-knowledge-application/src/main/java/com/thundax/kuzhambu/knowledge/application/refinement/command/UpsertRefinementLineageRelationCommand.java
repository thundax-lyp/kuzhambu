package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record UpsertRefinementLineageRelationCommand(
        Long refinementTaskId,
        Long relationId,
        String relationKey,
        String sourceNodeKey,
        String targetNodeKey,
        String sourceName,
        String targetName,
        String relationType,
        String evidence,
        String sourceRefsJson,
        Integer sortOrder,
        Long operatorId) {}
