package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public interface GraphPublishedEdgeRepository {
    GraphPublishedEdge getById(GraphPublishedEdgeId id);

    GraphPublishedEdge getByEdgeKey(GraphEdgeKey edgeKey);

    List<GraphPublishedEdge> listByNodeIds(List<GraphPublishedNodeId> nodeIds);

    List<GraphPublishedEdge> listByIds(List<GraphPublishedEdgeId> ids);

    List<GraphPublishedEdge> listRecentlyUpdated(int limit);

    List<GraphPublishedEdge> listRecentlyUpdatedByMaterials(
            List<com.thundax.kuzhambu.common.core.content.valueobject.ContentRef> materialRefs, int limit);

    GraphPublishedEdgeSlice listOneHopEdges(List<GraphPublishedNodeId> nodeIds, GraphPublishedEdgeId afterEdgeId);

    PageResult<GraphPublishedEdge> page(
            String keyword,
            String relationType,
            GraphPublishedStatus status,
            GraphSourceType source,
            int pageNo,
            int pageSize);

    GraphPublishedEdgeId insert(GraphPublishedEdge edge);

    int update(GraphPublishedEdge edge);

    int updateIfLockVersion(GraphPublishedEdge edge, long expectedLockVersion);

    long count(GraphPublishedStatus status);
}
