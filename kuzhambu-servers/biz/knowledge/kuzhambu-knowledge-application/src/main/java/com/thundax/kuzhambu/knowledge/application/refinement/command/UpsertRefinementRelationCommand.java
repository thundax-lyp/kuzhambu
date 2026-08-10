package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record UpsertRefinementRelationCommand(
        Long refinementTaskId,
        Long relationId,
        String relationKey,
        String sourceEntityKey,
        String targetEntityKey,
        String sourceName,
        String targetName,
        String relationType,
        String evidence,
        String sourceRefsJson,
        Integer sortOrder,
        Long operatorId) {}
