package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public interface GraphPublishedEdgeRepository {
    GraphPublishedEdge getById(GraphPublishedEdgeId id);

    GraphPublishedEdge getByEdgeKey(GraphEdgeKey edgeKey);

    List<GraphPublishedEdge> listByNodeIds(List<GraphPublishedNodeId> nodeIds);

    GraphPublishedEdgeSlice listIncidentEdges(
            List<GraphPublishedNodeId> nodeIds, GraphPublishedEdgeId afterEdgeId, int limit);

    int insert(GraphPublishedEdge edge);

    int update(GraphPublishedEdge edge);
}
