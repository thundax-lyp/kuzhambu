package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public interface GraphPublishedNodeRepository {
    GraphPublishedNode getById(GraphPublishedNodeId id);

    GraphPublishedNode getByNodeKey(GraphNodeKey nodeKey);

    List<GraphPublishedNode> listByIds(List<GraphPublishedNodeId> ids);

    int insert(GraphPublishedNode node);

    int update(GraphPublishedNode node);
}
