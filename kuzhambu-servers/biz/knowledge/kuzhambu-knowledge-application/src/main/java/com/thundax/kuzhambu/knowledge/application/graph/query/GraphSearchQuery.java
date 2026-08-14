package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;

public record GraphSearchQuery(String keyword, GraphNodeType nodeType, String relationType) {}
