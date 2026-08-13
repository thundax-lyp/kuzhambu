package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.PublishedGraphNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.PublishedGraphNodeId;

public interface PublishedGraphNodeRepository {

    PublishedGraphNode getById(PublishedGraphNodeId id);

    PublishedGraphNode getByNodeKey(String nodeKey);

    PublishedGraphNodeId insert(PublishedGraphNode node);

    int update(PublishedGraphNode node);
}
