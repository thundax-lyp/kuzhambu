package com.thundax.kuzhambu.knowledge.application.refinement.command;

public record UpsertRefinementLineageNodeCommand(
        Long refinementTaskId,
        Long nodeId,
        String nodeKey,
        String name,
        String nodeType,
        Integer generation,
        String gender,
        String sourceRefsJson,
        Integer sortOrder,
        Long operatorId) {}
