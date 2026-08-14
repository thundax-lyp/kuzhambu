package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphPublishedSearchHit;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphQualitySnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchMetrics;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedEdgeMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedNodeMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphWorkbenchMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphWorkbenchMapper.SearchHitRow;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class GraphWorkbenchRepositoryImpl implements GraphWorkbenchRepository {

    private static final String OBJECT_TYPE_NODE = "NODE";
    private static final String OBJECT_TYPE_EDGE = "EDGE";
    private static final String ISSUE_TYPE_ISOLATED = "ISOLATED";
    private static final String ISSUE_TYPE_MISSING_CORE_RELATION = "MISSING_CORE_RELATION";

    private final GraphWorkbenchMapper mapper;
    private final GraphPublishedNodeMapper nodeMapper;
    private final GraphPublishedEdgeMapper edgeMapper;

    public GraphWorkbenchRepositoryImpl(
            GraphWorkbenchMapper mapper, GraphPublishedNodeMapper nodeMapper, GraphPublishedEdgeMapper edgeMapper) {
        this.mapper = mapper;
        this.nodeMapper = nodeMapper;
        this.edgeMapper = edgeMapper;
    }

    @Override
    public GraphWorkbenchMetrics getByOverview() {
        return new GraphWorkbenchMetrics(
                mapper.countActiveNodes(),
                mapper.countActiveEdges(),
                mapper.countCoveredMaterials(),
                mapper.countIsolatedNodes(null),
                mapper.countMissingCoreRelationNodes(null));
    }

    @Override
    public PageResult<GraphPublishedSearchHit> page(
            String keyword, GraphNodeType nodeType, String relationType, int pageNo, int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        String nodeTypeValue = nodeType == null ? null : nodeType.value();
        long total = mapper.countSearchHits(keyword, nodeTypeValue, relationType);
        List<SearchHitRow> rows = mapper.searchHits(
                keyword, nodeTypeValue, relationType, offset(effectivePageNo, effectivePageSize), effectivePageSize);
        Map<Long, GraphPublishedNode> nodes = loadNodes(rows);
        Map<Long, GraphPublishedEdge> edges = loadEdges(rows);
        List<GraphPublishedSearchHit> records =
                rows.stream().map(row -> toSearchHit(row, nodes, edges)).toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public GraphQualitySnapshot getByQuality(String issueType, GraphNodeType nodeType, int sampleLimit) {
        int effectiveLimit = sampleLimit <= 0 ? 20 : sampleLimit;
        String nodeTypeValue = nodeType == null ? null : nodeType.value();
        long isolatedNodeCount = mapper.countIsolatedNodes(nodeTypeValue);
        long missingCoreRelationNodeCount = mapper.countMissingCoreRelationNodes(nodeTypeValue);
        List<GraphPublishedNode> isolatedNodes = shouldIncludeIssue(issueType, ISSUE_TYPE_ISOLATED)
                ? mapper.listIsolatedNodes(nodeTypeValue, effectiveLimit).stream()
                        .map(GraphPersistenceAssembler::toDomain)
                        .toList()
                : List.of();
        List<GraphPublishedNode> missingCoreRelationNodes =
                shouldIncludeIssue(issueType, ISSUE_TYPE_MISSING_CORE_RELATION)
                        ? mapper.listMissingCoreRelationNodes(nodeTypeValue, effectiveLimit).stream()
                                .map(GraphPersistenceAssembler::toDomain)
                                .toList()
                        : List.of();
        return new GraphQualitySnapshot(
                isolatedNodeCount, missingCoreRelationNodeCount, isolatedNodes, missingCoreRelationNodes);
    }

    private Map<Long, GraphPublishedNode> loadNodes(List<SearchHitRow> rows) {
        List<Long> ids = rows.stream()
                .filter(row -> OBJECT_TYPE_NODE.equals(row.getObjectType()))
                .map(SearchHitRow::getObjectId)
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, GraphPublishedNode> nodes = new HashMap<>();
        nodeMapper
                .selectBatchIds(ids)
                .forEach(node -> nodes.put(node.getId(), GraphPersistenceAssembler.toDomain(node)));
        return nodes;
    }

    private Map<Long, GraphPublishedEdge> loadEdges(List<SearchHitRow> rows) {
        List<Long> ids = rows.stream()
                .filter(row -> OBJECT_TYPE_EDGE.equals(row.getObjectType()))
                .map(SearchHitRow::getObjectId)
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, GraphPublishedEdge> edges = new HashMap<>();
        edgeMapper
                .selectBatchIds(ids)
                .forEach(edge -> edges.put(edge.getId(), GraphPersistenceAssembler.toDomain(edge)));
        return edges;
    }

    private GraphPublishedSearchHit toSearchHit(
            SearchHitRow row, Map<Long, GraphPublishedNode> nodes, Map<Long, GraphPublishedEdge> edges) {
        if (OBJECT_TYPE_NODE.equals(row.getObjectType())) {
            return new GraphPublishedSearchHit(row.getObjectType(), nodes.get(row.getObjectId()), null);
        }
        return new GraphPublishedSearchHit(row.getObjectType(), null, edges.get(row.getObjectId()));
    }

    private boolean shouldIncludeIssue(String requestedIssueType, String issueType) {
        return isBlank(requestedIssueType) || issueType.equals(requestedIssueType);
    }

    private int offset(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
