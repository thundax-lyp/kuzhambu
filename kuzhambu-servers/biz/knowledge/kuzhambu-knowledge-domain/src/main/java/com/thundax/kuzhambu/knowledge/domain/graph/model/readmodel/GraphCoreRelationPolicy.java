package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

import java.util.List;

/** Read-only core-relation rule supplied by the governed graph schema. */
public record GraphCoreRelationPolicy(String nodeType, List<String> relationTypes) {

    public GraphCoreRelationPolicy {
        relationTypes = relationTypes == null ? List.of() : List.copyOf(relationTypes);
    }
}
