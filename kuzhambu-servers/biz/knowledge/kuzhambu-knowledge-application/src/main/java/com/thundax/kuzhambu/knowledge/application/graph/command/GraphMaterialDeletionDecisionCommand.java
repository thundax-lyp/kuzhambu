package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionDecision;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;

public record GraphMaterialDeletionDecisionCommand(
        GraphMaterialDeletionChangeId changeId, GraphMaterialDeletionDecision decision, long lockVersion) {}
