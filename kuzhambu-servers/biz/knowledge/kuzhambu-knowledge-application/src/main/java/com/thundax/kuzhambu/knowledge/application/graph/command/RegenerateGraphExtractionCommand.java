package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public record RegenerateGraphExtractionCommand(
        String taskType,
        GraphExtractionTaskId sourceTaskId,
        String triggerSource,
        String selectionScopeJson,
        Boolean replaceUnconfirmedOnly,
        Long requestedBy) {}
