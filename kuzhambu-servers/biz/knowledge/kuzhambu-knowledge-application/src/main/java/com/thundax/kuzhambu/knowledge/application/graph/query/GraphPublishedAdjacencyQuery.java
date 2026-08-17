package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;

public record GraphPublishedAdjacencyQuery(
        String subjectKeyword,
        GraphNodeType subjectType,
        GraphPublishedStatus subjectStatus,
        GraphSourceType subjectSource,
        String relationType,
        GraphPublishedStatus relationStatus,
        GraphSourceType relationSource,
        String objectKeyword,
        GraphNodeType objectType,
        GraphPublishedStatus objectStatus,
        GraphSourceType objectSource,
        Boolean includeIsolated) {}
