package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphIncidentEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphQualityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphSearchQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphIncidentEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphQualityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphSearchResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWorkbenchOverviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphQualitySnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchMetrics;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GraphWorkbenchApplicationServiceImpl implements GraphWorkbenchApplicationService {

    private static final int RECENT_SEED_NODE_LIMIT = 100;
    private static final int LOCAL_GRAPH_NODE_LIMIT = 200;
    private static final int QUALITY_SAMPLE_LIMIT = 100;
    private static final String ISSUE_TYPE_ISOLATED_NODE = "ISOLATED_NODE";
    private static final String ISSUE_TYPE_MISSING_CORE_RELATION = "MISSING_CORE_RELATION";

    private final GraphWorkbenchRepository workbenchRepository;
    private final GraphPublishedNodeRepository nodeRepository;
    private final GraphPublishedEdgeRepository edgeRepository;

    public GraphWorkbenchApplicationServiceImpl(
            GraphWorkbenchRepository workbenchRepository,
            GraphPublishedNodeRepository nodeRepository,
            GraphPublishedEdgeRepository edgeRepository) {
        this.workbenchRepository = workbenchRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
    }

    @Override
    public GraphWorkbenchOverviewResult getOverview() {
        GraphWorkbenchMetrics metrics = workbenchRepository.getByOverview();
        return new GraphWorkbenchOverviewResult(
                metrics.publishedNodeCount(),
                metrics.publishedEdgeCount(),
                metrics.coveredMaterialCount(),
                metrics.isolatedNodeCount(),
                metrics.missingCoreRelationNodeCount());
    }

    @Override
    public List<GraphPublishedNode> listRecentSeedNodes() {
        return nodeRepository.listRecentlyUpdated(RECENT_SEED_NODE_LIMIT);
    }

    @Override
    public GraphIncidentEdgesResult listIncidentEdges(GraphIncidentEdgesQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        List<GraphPublishedNodeId> seedNodeIds = query == null || query.nodeIds() == null ? List.of() : query.nodeIds();
        GraphPublishedEdgeSlice edgeSlice = edgeRepository.listIncidentEdges(
                seedNodeIds, query == null ? null : query.afterEdgeId(), effectivePage.getPageSize());
        return GraphApplicationAssembler.toIncidentEdgesResult(localGraphNodes(seedNodeIds, edgeSlice), edgeSlice);
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
                issueType, query == null ? null : query.nodeType(), QUALITY_SAMPLE_LIMIT);
        return new GraphQualityResult(
                snapshot.isolatedNodeCount(),
                snapshot.missingCoreRelationNodeCount(),
                snapshot.isolatedNodes(),
                snapshot.missingCoreRelationNodes());
    }

    private List<GraphPublishedNode> localGraphNodes(
            List<GraphPublishedNodeId> seedNodeIds, GraphPublishedEdgeSlice edgeSlice) {
        Map<GraphPublishedNodeId, GraphPublishedNode> nodesById = new LinkedHashMap<>();
        for (GraphPublishedNode node : nodeRepository.listByIds(seedNodeIds)) {
            putActiveNode(nodesById, node);
        }
        for (GraphPublishedNode node : nodeRepository.listByIds(edgeNodeIds(edgeSlice))) {
            putActiveNode(nodesById, node);
            if (nodesById.size() >= LOCAL_GRAPH_NODE_LIMIT) {
                break;
            }
        }
        return nodesById.values().stream().limit(LOCAL_GRAPH_NODE_LIMIT).toList();
    }

    private List<GraphPublishedNodeId> edgeNodeIds(GraphPublishedEdgeSlice edgeSlice) {
        if (edgeSlice == null || edgeSlice.edges() == null) {
            return List.of();
        }
        return edgeSlice.edges().stream()
                .flatMap(edge -> List.of(edge.getSourceNodeId(), edge.getTargetNodeId()).stream())
                .distinct()
                .toList();
    }

    private void putActiveNode(Map<GraphPublishedNodeId, GraphPublishedNode> nodesById, GraphPublishedNode node) {
        if (node != null
                && node.getStatus() == GraphPublishedStatus.ACTIVE
                && nodesById.size() < LOCAL_GRAPH_NODE_LIMIT) {
            nodesById.putIfAbsent(node.getId(), node);
        }
    }
}
