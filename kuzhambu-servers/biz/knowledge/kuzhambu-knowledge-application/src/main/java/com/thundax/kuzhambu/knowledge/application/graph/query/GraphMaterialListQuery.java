package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;

public record GraphMaterialListQuery(String keyword, GraphMaterialStatus status) {}
