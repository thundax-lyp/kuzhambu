package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;

public record GraphQualityQuery(String issueType, GraphNodeType nodeType) {}
