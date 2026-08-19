package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphOneHopEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphQualityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphSearchQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphOneHopEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphQualityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphRecentEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphSearchResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWorkbenchOverviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphQualitySnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchSnapshotStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GraphWorkbenchApplicationServiceImpl implements GraphWorkbenchApplicationService {

    private static final int RECENT_EDGE_LIMIT = 200;
    private static final int QUALITY_SAMPLE_LIMIT = 100;
    private static final String ISSUE_TYPE_ISOLATED_NODE = "ISOLATED_NODE";
    private static final String ISSUE_TYPE_MISSING_CORE_RELATION = "MISSING_CORE_RELATION";

    private final GraphWorkbenchRepository workbenchRepository;
    private final GraphPublishedNodeRepository nodeRepository;
    private final GraphPublishedEdgeRepository edgeRepository;
    private final GraphSchemaResolver schemaResolver;
    private final GraphWorkbenchSnapshotStore snapshotStore;

    public GraphWorkbenchApplicationServiceImpl(
            GraphWorkbenchRepository workbenchRepository,
            GraphPublishedNodeRepository nodeRepository,
            GraphPublishedEdgeRepository edgeRepository,
            GraphSchemaResolver schemaResolver,
            GraphWorkbenchSnapshotStore snapshotStore) {
        this.workbenchRepository = workbenchRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.schemaResolver = schemaResolver;
        this.snapshotStore = snapshotStore;
    }

    @Override
    public GraphWorkbenchOverviewResult getOverview() {
        GraphWorkbenchOverviewSnapshot snapshot = snapshotStore
                .get()
                .orElseThrow(() -> new BizException(
                        "WORKBENCH_SNAPSHOT_UNAVAILABLE",
                        "knowledge.graph.workbench-snapshot-unavailable",
                        "Graph workbench overview snapshot is unavailable"));
        return new GraphWorkbenchOverviewResult(
                snapshot.generatedAt(),
                snapshot.publishedNodeCount(),
                snapshot.publishedEdgeCount(),
                snapshot.coveredMaterialCount(),
                snapshot.isolatedNodeCount(),
                snapshot.missingCoreRelationNodeCount(),
                snapshot.recentActivities(),
                snapshot.pendingConflictCount());
    }

    @Override
    public GraphRecentEdgesResult listRecentEdges() {
        List<GraphPublishedEdge> recentEdges = edgeRepository.listRecentlyUpdated(RECENT_EDGE_LIMIT);
        Map<GraphPublishedNodeId, GraphPublishedNode> nodesById = new LinkedHashMap<>();
        for (GraphPublishedNode node : nodeRepository.listByIds(edgeNodeIds(recentEdges))) {
            if (node != null && node.getStatus() == GraphPublishedStatus.ACTIVE) {
                nodesById.putIfAbsent(node.getId(), node);
            }
        }
        List<GraphPublishedEdge> acceptedEdges = recentEdges.stream()
                .filter(edge ->
                        nodesById.containsKey(edge.getSourceNodeId()) && nodesById.containsKey(edge.getTargetNodeId()))
                .toList();
        List<GraphPublishedNodeId> acceptedNodeIds = edgeNodeIds(acceptedEdges);
        return new GraphRecentEdgesResult(
                acceptedNodeIds.stream().map(nodesById::get).toList(), acceptedEdges);
    }

    @Override
    public GraphOneHopEdgesResult listOneHopEdges(GraphOneHopEdgesQuery query) {
        List<GraphPublishedNodeId> nodeIds = query == null || query.nodeIds() == null ? List.of() : query.nodeIds();
        GraphPublishedEdgeSlice edgeSlice =
                edgeRepository.listOneHopEdges(nodeIds, query == null ? null : query.afterEdgeId());
        List<GraphPublishedEdge> edges = edgeSlice == null ? List.of() : edgeSlice.edges();
        Map<GraphPublishedNodeId, GraphPublishedNode> nodesById = activeNodesById(edges);
        List<GraphPublishedEdge> acceptedEdges = edgesWithActiveEndpoints(edges, nodesById);
        return new GraphOneHopEdgesResult(
                edgeNodeIds(acceptedEdges).stream().map(nodesById::get).toList(),
                acceptedEdges,
                edgeSlice.nextCursor(),
                edgeSlice.truncated());
    }

    @Override
    public PageResult<GraphSearchResult> search(GraphSearchQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<GraphSearchResult> result = new PageResult<>();
        PageResult<com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphPublishedSearchHit> source =
                workbenchRepository.page(
                        query == null ? null : query.keyword(),
                        query == null ? null : query.nodeType(),
                        query == null ? null : query.relationType(),
                        effectivePage.getPageNo(),
                        effectivePage.getPageSize());
        result.setPageNo(source.getPageNo());
        result.setPageSize(source.getPageSize());
        result.setTotalCount(source.getTotalCount());
        result.setTotalPage(source.getTotalPage());
        result.setRecords(source.getRecords().stream()
                .map(GraphApplicationAssembler::toSearchResult)
                .toList());
        return result;
    }

    @Override
    public GraphQualityResult getQuality(GraphQualityQuery query) {
        String issueType = query == null ? null : query.issueType();
        if (issueType != null
                && !ISSUE_TYPE_ISOLATED_NODE.equals(issueType)
                && !ISSUE_TYPE_MISSING_CORE_RELATION.equals(issueType)) {
            throw new BizException("Unsupported graph quality issue type");
        }
        GraphQualitySnapshot snapshot = workbenchRepository.getByQuality(
                issueType,
                query == null ? null : query.nodeType(),
                QUALITY_SAMPLE_LIMIT,
                schemaResolver.coreRelationPolicies());
        return new GraphQualityResult(
                snapshot.isolatedNodeCount(),
                snapshot.missingCoreRelationNodeCount(),
                snapshot.isolatedNodes(),
                snapshot.missingCoreRelationNodes());
    }

    private Map<GraphPublishedNodeId, GraphPublishedNode> activeNodesById(List<GraphPublishedEdge> edges) {
        Map<GraphPublishedNodeId, GraphPublishedNode> nodesById = new LinkedHashMap<>();
        for (GraphPublishedNode node : nodeRepository.listByIds(edgeNodeIds(edges))) {
            if (node != null && node.getStatus() == GraphPublishedStatus.ACTIVE) {
                nodesById.putIfAbsent(node.getId(), node);
            }
        }
        return nodesById;
    }

    private List<GraphPublishedEdge> edgesWithActiveEndpoints(
            List<GraphPublishedEdge> edges, Map<GraphPublishedNodeId, GraphPublishedNode> nodesById) {
        return edges.stream()
                .filter(edge ->
                        nodesById.containsKey(edge.getSourceNodeId()) && nodesById.containsKey(edge.getTargetNodeId()))
                .toList();
    }

    private List<GraphPublishedNodeId> edgeNodeIds(GraphPublishedEdgeSlice edgeSlice) {
        return edgeNodeIds(edgeSlice == null ? null : edgeSlice.edges());
    }

    private List<GraphPublishedNodeId> edgeNodeIds(List<GraphPublishedEdge> edges) {
        if (edges == null) {
            return List.of();
        }
        return edges.stream()
                .flatMap(edge -> List.of(edge.getSourceNodeId(), edge.getTargetNodeId()).stream())
                .distinct()
                .toList();
    }
}
