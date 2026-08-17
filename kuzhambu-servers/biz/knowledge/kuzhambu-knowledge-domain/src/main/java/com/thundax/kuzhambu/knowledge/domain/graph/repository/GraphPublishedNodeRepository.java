package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphPublishedAdjacency;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public interface GraphPublishedNodeRepository {
    GraphPublishedNode getById(GraphPublishedNodeId id);

    GraphPublishedNode getByNodeKey(GraphNodeKey nodeKey);

    List<GraphPublishedNode> listByIds(List<GraphPublishedNodeId> ids);

    List<GraphPublishedNode> listRecentlyUpdated(int limit);

    PageResult<GraphPublishedNode> page(
            String keyword,
            GraphNodeType nodeType,
            GraphPublishedStatus status,
            GraphSourceType source,
            int pageNo,
            int pageSize);

    PageResult<GraphPublishedAdjacency> pageAdjacency(
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
            boolean includeIsolated,
            int pageNo,
            int pageSize);

    GraphPublishedNodeId insert(GraphPublishedNode node);

    int update(GraphPublishedNode node);

    int updateIfLockVersion(GraphPublishedNode node, long expectedLockVersion);

    long count(GraphPublishedStatus status);
}
