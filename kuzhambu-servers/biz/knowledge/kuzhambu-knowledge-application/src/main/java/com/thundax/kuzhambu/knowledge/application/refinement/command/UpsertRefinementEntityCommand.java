package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record UpsertRefinementEntityCommand(
        Long refinementTaskId,
        Long entityId,
        String entityKey,
        String name,
        String entityType,
        String description,
        String sourceRefsJson,
        Integer sortOrder,
        Long operatorId) {}
