package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedAdjacencyQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeDeleteQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeDeleteQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphGovernanceImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedAdjacencyResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedEdgeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedNodeDetailResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;

public interface GraphPublishedApplicationService {

    PageResult<GraphPublishedNode> pageNodes(GraphPublishedNodeQuery query, PageQuery pageQuery);

    PageResult<GraphPublishedEdge> pageEdges(GraphPublishedEdgeQuery query, PageQuery pageQuery);

    PageResult<GraphPublishedAdjacencyResult> pageAdjacency(GraphPublishedAdjacencyQuery query, PageQuery pageQuery);

    GraphPublishedNodeDetailResult getNodeDetail(GraphPublishedNodeId nodeId);

    GraphPublishedEdgeDetailResult getEdgeDetail(GraphPublishedEdgeId edgeId);

    GraphPublishedNodeId createNode(GraphPublishedNodeCommand command);

    void updateNode(GraphPublishedNodeCommand command);

    GraphGovernanceImpactResult previewNodeDeletion(GraphPublishedNodeDeleteQuery query);

    void deleteNode(GraphPublishedNodeDeleteCommand command);

    GraphPublishedEdgeId createEdge(GraphPublishedEdgeCommand command);

    void updateEdge(GraphPublishedEdgeCommand command);

    GraphGovernanceImpactResult previewEdgeDeletion(GraphPublishedEdgeDeleteQuery query);

    void deleteEdge(GraphPublishedEdgeDeleteCommand command);

    GraphGovernanceImpactResult previewNodeMerge(GraphPublishedNodeMergeQuery query);

    GraphPublishedNodeDetailResult mergeNodes(GraphPublishedNodeMergeCommand command);

    GraphGovernanceImpactResult previewNodeSplit(GraphPublishedNodeSplitQuery query);

    GraphPublishedNodeDetailResult splitNode(GraphPublishedNodeSplitCommand command);
}
