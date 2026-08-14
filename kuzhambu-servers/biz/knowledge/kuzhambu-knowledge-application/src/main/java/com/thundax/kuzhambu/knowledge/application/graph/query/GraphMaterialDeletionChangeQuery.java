package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;

public record GraphMaterialDeletionChangeQuery(GraphMaterialDeletionStatus status) {}
