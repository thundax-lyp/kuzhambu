package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublishedNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeDeleteQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedEdgeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeDeleteQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublishedNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphGovernanceImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphGovernanceOperationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedEdgeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedNodeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublishedApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphPublishedMutationSet;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphPublishedSubgraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceImpactToken;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceOperation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphManualSource;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphGovernanceImpactTokenRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphGovernanceOperationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphManualSourceRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodePropertyRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import com.thundax.kuzhambu.system.facade.SystemAuditFacade;
import com.thundax.kuzhambu.system.facade.request.SystemAuditFacadeRequest;
import com.thundax.kuzhambu.system.facade.response.SystemAuditFacadeResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraphPublishedApplicationServiceImpl implements GraphPublishedApplicationService {

    private static final String ISSUE_GOVERNANCE_FAILED = "GRAPH_GOVERNANCE_FAILED";
    private static final String SEVERITY_BLOCKING = "BLOCKING";
    private static final String TARGET_NODE = "NODE";
    private static final String TARGET_EDGE = "EDGE";
    private static final String AUDIT_OBJECT_PREFIX = "KNOWLEDGE_GRAPH_";
    private static final String AUDIT_SOURCE = "KNOWLEDGE_GRAPH_GOVERNANCE";
    private static final Duration IMPACT_TOKEN_TTL = Duration.ofMinutes(15);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GraphPublishedNodeRepository nodeRepository;
    private final GraphPublishedEdgeRepository edgeRepository;
    private final GraphPublishedNodePropertyRepository nodePropertyRepository;
    private final GraphPublishedEdgePropertyRepository edgePropertyRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;
    private final GraphGovernanceImpactTokenRepository impactTokenRepository;
    private final GraphGovernanceOperationRepository governanceOperationRepository;
    private final GraphManualSourceRepository manualSourceRepository;
    private final SystemAuditFacade auditFacade;
    private final GraphSchemaResolver schemaSupport;
    private final Clock clock;

    public GraphPublishedApplicationServiceImpl(
            GraphPublishedNodeRepository nodeRepository,
            GraphPublishedEdgeRepository edgeRepository,
            GraphPublishedNodePropertyRepository nodePropertyRepository,
            GraphPublishedEdgePropertyRepository edgePropertyRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository,
            GraphGovernanceImpactTokenRepository impactTokenRepository,
            GraphGovernanceOperationRepository governanceOperationRepository,
            GraphManualSourceRepository manualSourceRepository,
            SystemAuditFacade auditFacade,
            GraphSchemaResolver schemaSupport,
            Clock clock) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.nodePropertyRepository = nodePropertyRepository;
        this.edgePropertyRepository = edgePropertyRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
        this.impactTokenRepository = impactTokenRepository;
        this.governanceOperationRepository = governanceOperationRepository;
        this.manualSourceRepository = manualSourceRepository;
        this.auditFacade = auditFacade;
        this.schemaSupport = schemaSupport;
        this.clock = clock;
    }

    @Override
    public PageResult<GraphPublishedNode> pageNodes(GraphPublishedNodeQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        return nodeRepository.page(
                query == null ? null : query.keyword(),
                query == null ? null : query.nodeType(),
                query == null ? null : query.status(),
                query == null ? null : query.source(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
    }

    @Override
    public PageResult<GraphPublishedEdge> pageEdges(GraphPublishedEdgeQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        return edgeRepository.page(
                query == null ? null : query.keyword(),
                query == null ? null : query.relationType(),
                query == null ? null : query.status(),
                query == null ? null : query.source(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
    }

    @Override
    public GraphPublishedNodeDetailResult getNodeDetail(GraphPublishedNodeId nodeId) {
        GraphPublishedNode node = requireNode(nodeId);
        return toNodeDetail(node);
    }

    @Override
    public GraphPublishedEdgeDetailResult getEdgeDetail(GraphPublishedEdgeId edgeId) {
        GraphPublishedEdge edge = requireEdge(edgeId);
        return toEdgeDetail(edge);
    }

    @Override
    @Transactional
    public GraphPublishedNodeId createNode(GraphPublishedNodeCommand command) {
        GraphPublishedNode node = requireCommandNode(command);
        if (node.getId() != null) {
            throw new BizException("Graph published node id must be empty when creating");
        }
        if (node.getSource() != GraphSourceType.MANUAL) {
            throw new BizException("Graph published node source must be MANUAL when creating");
        }
        node.setStatus(GraphPublishedStatus.ACTIVE);
        node.touch(now());
        refreshNodeKey(node);
        node.setId(nodeRepository.insert(node));
        replaceNodeProperties(node.getId(), command.properties());
        recordGovernanceOperation(
                "CREATE",
                TARGET_NODE,
                node.getId().value(),
                command.reason(),
                null,
                nodeSnapshot(node, command.properties()));
        return node.getId();
    }

    @Override
    @Transactional
    public void updateNode(GraphPublishedNodeCommand command) {
        GraphPublishedNode node = requireCommandNode(command);
        GraphPublishedNode existing = requireNode(node.getId());
        existing.requireLockVersion(node.getLockVersion());
        String beforeSnapshot = nodeSnapshot(existing, nodePropertyRepository.listByPublishedNodeId(existing.getId()));
        if (existing.getStatus() == GraphPublishedStatus.DELETED && node.getStatus() == GraphPublishedStatus.ACTIVE) {
            existing.activate(now());
        } else if (existing.getStatus() != GraphPublishedStatus.ACTIVE) {
            throw new BizException("Only active graph published nodes can be updated");
        }
        existing.setNodeType(node.getNodeType());
        existing.setName(node.getName());
        existing.setSource(node.getSource());
        existing.setStatus(GraphPublishedStatus.ACTIVE);
        existing.touch(now());
        refreshNodeKey(existing);
        updateNodeCas(existing, node.getLockVersion());
        replaceNodeProperties(existing.getId(), command.properties());
        refreshIncidentEdgeKeys(existing.getId());
        recordGovernanceOperation(
                "UPDATE",
                TARGET_NODE,
                existing.getId().value(),
                command.reason(),
                beforeSnapshot,
                nodeSnapshot(existing, command.properties()));
    }

    @Override
    public GraphGovernanceImpactResult previewNodeDeletion(GraphPublishedNodeDeleteQuery query) {
        try {
            GraphPublishedNodeId nodeId = query == null ? null : query.nodeId();
            GraphPublishedNode node = requireNode(nodeId);
            List<GraphPublishedEdge> edges = edgeRepository.listByNodeIds(List.of(nodeId));
            GraphPublishedMutationSet changes =
                    new GraphPublishedSubgraph(List.of(node), edges).deleteNode(nodeId, query.cascadeEdges(), now());
            return withImpactToken(
                    "NODE_DELETE",
                    toImpact(changes, nodeMaterials(List.of(nodeId)), edgeMaterials(edges), List.of(), true));
        } catch (RuntimeException ex) {
            return failedImpact(ex);
        }
    }

    @Override
    @Transactional
    public void deleteNode(GraphPublishedNodeDeleteCommand command) {
        GraphPublishedNode node = requireNode(command.nodeId());
        node.requireLockVersion(command.nodeLockVersion());
        String beforeSnapshot = nodeSnapshot(node, nodePropertyRepository.listByPublishedNodeId(node.getId()));
        List<GraphPublishedEdge> edges = edgeRepository.listByNodeIds(List.of(command.nodeId()));
        GraphPublishedMutationSet changes = new GraphPublishedSubgraph(List.of(node), edges)
                .deleteNode(command.nodeId(), command.cascadeEdges(), now());
        validateImpactToken(
                command.impactToken(),
                "NODE_DELETE",
                toImpact(changes, nodeMaterials(List.of(command.nodeId())), edgeMaterials(edges), List.of(), true));
        updateEdgesCas(changes.updatedEdges());
        updateNodesCas(changes.updatedNodes(), Map.of(command.nodeId(), command.nodeLockVersion()));
        recordGovernanceOperation(
                "DELETE",
                TARGET_NODE,
                command.nodeId().value(),
                command.reason(),
                beforeSnapshot,
                nodeSnapshot(node, nodePropertyRepository.listByPublishedNodeId(node.getId())));
        impactTokenRepository.updateConsumedAtIfAvailable(command.impactToken(), now());
    }

    @Override
    @Transactional
    public GraphPublishedEdgeId createEdge(GraphPublishedEdgeCommand command) {
        GraphPublishedEdge edge = requireCommandEdge(command);
        if (edge.getId() != null) {
            throw new BizException("Graph published edge id must be empty when creating");
        }
        if (edge.getSource() != GraphSourceType.MANUAL) {
            throw new BizException("Graph published edge source must be MANUAL when creating");
        }
        edge.setStatus(GraphPublishedStatus.ACTIVE);
        edge.touch(now());
        refreshEdgeKey(edge);
        edge.setId(edgeRepository.insert(edge));
        replaceEdgeProperties(edge.getId(), command.properties());
        recordGovernanceOperation(
                "CREATE",
                TARGET_EDGE,
                edge.getId().value(),
                command.reason(),
                null,
                edgeSnapshot(edge, command.properties()));
        return edge.getId();
    }

    @Override
    @Transactional
    public void updateEdge(GraphPublishedEdgeCommand command) {
        GraphPublishedEdge edge = requireCommandEdge(command);
        GraphPublishedEdge existing = requireEdge(edge.getId());
        existing.requireLockVersion(edge.getLockVersion());
        String beforeSnapshot = edgeSnapshot(existing, edgePropertyRepository.listByPublishedEdgeId(existing.getId()));
        if (existing.getStatus() == GraphPublishedStatus.DELETED && edge.getStatus() == GraphPublishedStatus.ACTIVE) {
            existing.activate(now());
        } else if (existing.getStatus() != GraphPublishedStatus.ACTIVE) {
            throw new BizException("Only active graph published edges can be updated");
        }
        existing.setSourceNodeId(edge.getSourceNodeId());
        existing.setTargetNodeId(edge.getTargetNodeId());
        existing.setRelationType(edge.getRelationType());
        existing.setSource(edge.getSource());
        existing.setQualifiersJson(edge.getQualifiersJson());
        existing.setStatus(GraphPublishedStatus.ACTIVE);
        existing.touch(now());
        refreshEdgeKey(existing);
        updateEdgeCas(existing, edge.getLockVersion());
        replaceEdgeProperties(existing.getId(), command.properties());
        recordGovernanceOperation(
                "UPDATE",
                TARGET_EDGE,
                existing.getId().value(),
                command.reason(),
                beforeSnapshot,
                edgeSnapshot(existing, command.properties()));
    }

    @Override
    public GraphGovernanceImpactResult previewEdgeDeletion(GraphPublishedEdgeDeleteQuery query) {
        try {
            GraphPublishedEdge edge = requireEdge(query == null ? null : query.edgeId());
            GraphPublishedMutationSet changes =
                    new GraphPublishedSubgraph(List.of(), List.of(edge)).deleteEdge(edge.getId(), now());
            return withImpactToken(
                    "EDGE_DELETE", toImpact(changes, List.of(), edgeMaterials(List.of(edge)), List.of(), true));
        } catch (RuntimeException ex) {
            return failedImpact(ex);
        }
    }

    @Override
    @Transactional
    public void deleteEdge(GraphPublishedEdgeDeleteCommand command) {
        GraphPublishedEdge edge = requireEdge(command.edgeId());
        edge.requireLockVersion(command.edgeLockVersion());
        String beforeSnapshot = edgeSnapshot(edge, edgePropertyRepository.listByPublishedEdgeId(edge.getId()));
        GraphPublishedMutationSet changes =
                new GraphPublishedSubgraph(List.of(), List.of(edge)).deleteEdge(command.edgeId(), now());
        validateImpactToken(
                command.impactToken(),
                "EDGE_DELETE",
                toImpact(changes, List.of(), edgeMaterials(List.of(edge)), List.of(), true));
        updateEdgesCas(changes.updatedEdges(), Map.of(command.edgeId(), command.edgeLockVersion()));
        recordGovernanceOperation(
                "DELETE",
                TARGET_EDGE,
                command.edgeId().value(),
                command.reason(),
                beforeSnapshot,
                edgeSnapshot(edge, edgePropertyRepository.listByPublishedEdgeId(edge.getId())));
        impactTokenRepository.updateConsumedAtIfAvailable(command.impactToken(), now());
    }

    @Override
    public GraphGovernanceImpactResult previewNodeMerge(GraphPublishedNodeMergeQuery query) {
        try {
            GraphPublishedMutationSet changes = planMerge(query.retainedNodeId(), query.mergedNodeIds(), false);
            return withImpactToken(
                    "NODE_MERGE",
                    toImpact(
                            changes,
                            mergeNodeMaterialSnapshot(query.retainedNodeId(), query.mergedNodeIds()),
                            mergeEdgeMaterials(changes.updatedEdges()),
                            List.of(),
                            true));
        } catch (RuntimeException ex) {
            return failedImpact(ex);
        }
    }

    @Override
    @Transactional
    public GraphPublishedNodeDetailResult mergeNodes(GraphPublishedNodeMergeCommand command) {
        GraphPublishedNode retainedNode = requireNode(command.retainedNodeId());
        retainedNode.requireLockVersion(command.retainedNodeLockVersion());
        String beforeSnapshot =
                nodeSnapshot(retainedNode, nodePropertyRepository.listByPublishedNodeId(retainedNode.getId()));
        GraphPublishedMutationSet previewChanges = planMerge(command.retainedNodeId(), command.mergedNodeIds(), false);
        validateImpactToken(
                command.impactToken(),
                "NODE_MERGE",
                toImpact(
                        previewChanges,
                        mergeNodeMaterialSnapshot(command.retainedNodeId(), command.mergedNodeIds()),
                        mergeEdgeMaterials(previewChanges.updatedEdges()),
                        List.of(),
                        true));
        mergeNodeProperties(command.retainedNodeId(), command.mergedNodeIds());
        mergeNodeMaterials(command.retainedNodeId(), command.mergedNodeIds());
        previewChanges.updatedEdges().forEach(this::refreshEdgeKey);
        Map<GraphPublishedNodeId, Long> expectedNodeVersions = new LinkedHashMap<>();
        expectedNodeVersions.put(command.retainedNodeId(), command.retainedNodeLockVersion());
        updateEdgesCas(deduplicateEdges(previewChanges.updatedEdges()));
        updateNodesCas(previewChanges.updatedNodes(), expectedNodeVersions);
        recordGovernanceOperation(
                "MERGE",
                TARGET_NODE,
                command.retainedNodeId().value(),
                command.reason(),
                beforeSnapshot,
                nodeSnapshot(retainedNode, nodePropertyRepository.listByPublishedNodeId(retainedNode.getId())));
        impactTokenRepository.updateConsumedAtIfAvailable(command.impactToken(), now());
        return getNodeDetail(command.retainedNodeId());
    }

    @Override
    public GraphGovernanceImpactResult previewNodeSplit(GraphPublishedNodeSplitQuery query) {
        GraphPublishedNodeId nodeId = query == null ? null : query.sourceNodeId();
        GraphPublishedNode node = requireNode(nodeId);
        List<GraphPublishedEdge> edges = edgeRepository.listByNodeIds(List.of(nodeId));
        return withImpactToken(
                "NODE_SPLIT",
                new GraphGovernanceImpactResult(
                        null,
                        List.of(node),
                        edges,
                        nodeMaterials(List.of(nodeId)),
                        edgeMaterials(edges),
                        List.of(),
                        true));
    }

    @Override
    @Transactional
    public GraphPublishedNodeDetailResult splitNode(GraphPublishedNodeSplitCommand command) {
        GraphPublishedNode sourceNode = requireNode(command.sourceNodeId());
        sourceNode.requireLockVersion(command.sourceNodeLockVersion());
        String beforeSnapshot =
                nodeSnapshot(sourceNode, nodePropertyRepository.listByPublishedNodeId(sourceNode.getId()));
        List<GraphPublishedEdge> previewEdges = edgeRepository.listByNodeIds(List.of(command.sourceNodeId()));
        validateImpactToken(
                command.impactToken(),
                "NODE_SPLIT",
                new GraphGovernanceImpactResult(
                        null,
                        List.of(sourceNode),
                        previewEdges,
                        nodeMaterials(List.of(command.sourceNodeId())),
                        edgeMaterials(previewEdges),
                        List.of(),
                        true));
        validateSplitCoverage(command, sourceNode);
        GraphPublishedNode splitNode = command.splitNode();
        if (splitNode == null || splitNode.getId() != null) {
            throw new BizException("Graph published split node must be new");
        }
        splitNode.setSource(GraphSourceType.MANUAL);
        splitNode.setStatus(GraphPublishedStatus.ACTIVE);
        splitNode.touch(now());
        refreshNodeKey(splitNode);
        splitNode.setId(nodeRepository.insert(splitNode));
        moveAndCopyNodeProperties(command, splitNode.getId());
        moveAndCopyNodeMaterials(command, splitNode.getId());
        GraphPublishedMutationSet changes = new GraphPublishedSubgraph(
                        List.of(sourceNode, splitNode), edgeRepository.listByNodeIds(List.of(command.sourceNodeId())))
                .splitNode(command.sourceNodeId(), splitNode, command.reassignedEdgeIds(), now());
        changes.updatedEdges().forEach(this::refreshEdgeKey);
        requireNoEdgeKeyConflicts(changes.updatedEdges());
        insertCopiedEdges(command.copiedEdges(), sourceNode, splitNode);
        updateEdgesCas(changes.updatedEdges());
        updateNodesCas(changes.updatedNodes(), Map.of(command.sourceNodeId(), command.sourceNodeLockVersion()));
        recordGovernanceOperation(
                "SPLIT",
                TARGET_NODE,
                splitNode.getId().value(),
                command.reason(),
                beforeSnapshot,
                nodeSnapshot(splitNode, nodePropertyRepository.listByPublishedNodeId(splitNode.getId())));
        impactTokenRepository.updateConsumedAtIfAvailable(command.impactToken(), now());
        return getNodeDetail(splitNode.getId());
    }

    private GraphPublishedMutationSet planMerge(
            GraphPublishedNodeId retainedNodeId, List<GraphPublishedNodeId> mergedNodeIds, boolean refreshEdgeKeys) {
        List<GraphPublishedNodeId> nodeIds = new ArrayList<>();
        nodeIds.add(retainedNodeId);
        nodeIds.addAll(safeNodeIds(mergedNodeIds));
        List<GraphPublishedNode> nodes = nodeRepository.listByIds(nodeIds);
        List<GraphPublishedEdge> edges = edgeRepository.listByNodeIds(nodeIds);
        GraphPublishedMutationSet changes =
                new GraphPublishedSubgraph(nodes, edges).mergeNodes(retainedNodeId, mergedNodeIds, now());
        if (refreshEdgeKeys) {
            changes.updatedEdges().forEach(this::refreshEdgeKey);
        }
        return changes;
    }

    private void refreshIncidentEdgeKeys(GraphPublishedNodeId nodeId) {
        for (GraphPublishedEdge edge : edgeRepository.listByNodeIds(List.of(nodeId))) {
            long expectedLockVersion = edge.getLockVersion();
            edge.touch(now());
            refreshEdgeKey(edge);
            updateEdgeCas(edge, expectedLockVersion);
        }
    }

    private List<GraphPublishedEdge> deduplicateEdges(List<GraphPublishedEdge> edges) {
        List<GraphPublishedEdge> updates = new ArrayList<>();
        Map<GraphEdgeKey, GraphPublishedEdge> retainedByKey = new LinkedHashMap<>();
        for (GraphPublishedEdge edge : edges == null ? List.<GraphPublishedEdge>of() : edges) {
            GraphPublishedEdge retained = retainedByKey.get(edge.getEdgeKey());
            if (retained == null) {
                retainedByKey.put(edge.getEdgeKey(), edge);
                updates.add(edge);
                continue;
            }
            mergeEdgeProperties(retained.getId(), edge.getId());
            mergeEdgeMaterials(retained.getId(), edge.getId());
            if (edge.getStatus() == GraphPublishedStatus.ACTIVE) {
                edge.delete(now());
            }
            updates.add(edge);
        }
        return updates;
    }

    private void insertCopiedEdges(
            List<GraphPublishedEdge> copiedEdges, GraphPublishedNode sourceNode, GraphPublishedNode splitNode) {
        for (GraphPublishedEdge edge : copiedEdges == null ? List.<GraphPublishedEdge>of() : copiedEdges) {
            if (edge.getId() != null) {
                throw new BizException("Graph published copied edge must be new");
            }
            if (sourceNode.getId().equals(edge.getSourceNodeId())) {
                edge.setSourceNodeId(splitNode.getId());
            }
            if (sourceNode.getId().equals(edge.getTargetNodeId())) {
                edge.setTargetNodeId(splitNode.getId());
            }
            edge.setSource(GraphSourceType.MANUAL);
            edge.setStatus(GraphPublishedStatus.ACTIVE);
            edge.touch(now());
            refreshEdgeKey(edge);
            edgeRepository.insert(edge);
        }
    }

    private void validateSplitCoverage(GraphPublishedNodeSplitCommand command, GraphPublishedNode sourceNode) {
        Set<GraphPublishedNodePropertyId> assignedProperties = new LinkedHashSet<>();
        assignedProperties.addAll(safePropertyIds(command.movedPropertyIds()));
        assignedProperties.addAll(safePropertyIds(command.copiedPropertyIds()));
        Set<GraphPublishedNodePropertyId> existingProperties =
                new LinkedHashSet<>(nodePropertyRepository.listByPublishedNodeId(sourceNode.getId()).stream()
                        .map(GraphPublishedNodeProperty::getId)
                        .toList());
        if (!assignedProperties.containsAll(existingProperties)
                || !existingProperties.containsAll(assignedProperties)) {
            throw new BizException("Graph published split must assign all source node properties");
        }
        Set<ContentRef> assignedMaterials = new LinkedHashSet<>();
        assignedMaterials.addAll(safeMaterialRefs(command.movedMaterialRefs()));
        assignedMaterials.addAll(safeMaterialRefs(command.copiedMaterialRefs()));
        Set<ContentRef> existingMaterials =
                new LinkedHashSet<>(nodeMaterialRepository.listByPublishedNodeId(sourceNode.getId()).stream()
                        .map(GraphPublishedNodeMaterial::getMaterialRef)
                        .toList());
        if (!assignedMaterials.containsAll(existingMaterials)) {
            throw new BizException("Graph published split must assign all source node materials");
        }
    }

    private void moveAndCopyNodeProperties(GraphPublishedNodeSplitCommand command, GraphPublishedNodeId splitNodeId) {
        for (GraphPublishedNodePropertyId propertyId : safePropertyIds(command.movedPropertyIds())) {
            GraphPublishedNodeProperty property = requireNodeProperty(propertyId);
            property.setPublishedNodeId(splitNodeId);
            nodePropertyRepository.update(property);
        }
        for (GraphPublishedNodePropertyId propertyId : safePropertyIds(command.copiedPropertyIds())) {
            GraphPublishedNodeProperty property = requireNodeProperty(propertyId);
            nodePropertyRepository.insert(new GraphPublishedNodeProperty(
                    null, splitNodeId, property.getPropertyKey(), property.getValue(), property.isPreferred()));
        }
    }

    private void moveAndCopyNodeMaterials(GraphPublishedNodeSplitCommand command, GraphPublishedNodeId splitNodeId) {
        List<GraphPublishedNodeMaterial> sourceMaterials =
                nodeMaterialRepository.listByPublishedNodeId(command.sourceNodeId());
        for (ContentRef materialRef : safeMaterialRefs(command.movedMaterialRefs())) {
            GraphPublishedNodeMaterial relation = requireNodeMaterial(sourceMaterials, materialRef);
            nodeMaterialRepository.deleteByPublishedNodeIdAndMaterialRef(command.sourceNodeId(), materialRef);
            nodeMaterialRepository.insert(new GraphPublishedNodeMaterial(
                    splitNodeId, relation.getMaterialRef(), relation.getSourceSnapshotJson()));
        }
        for (ContentRef materialRef : safeMaterialRefs(command.copiedMaterialRefs())) {
            GraphPublishedNodeMaterial relation = requireNodeMaterial(sourceMaterials, materialRef);
            nodeMaterialRepository.insert(new GraphPublishedNodeMaterial(
                    splitNodeId, relation.getMaterialRef(), relation.getSourceSnapshotJson()));
        }
    }

    private void mergeNodeProperties(GraphPublishedNodeId retainedNodeId, List<GraphPublishedNodeId> mergedNodeIds) {
        List<GraphPublishedNodeProperty> retained = nodePropertyRepository.listByPublishedNodeId(retainedNodeId);
        for (GraphPublishedNodeId mergedNodeId : safeNodeIds(mergedNodeIds)) {
            for (GraphPublishedNodeProperty property : nodePropertyRepository.listByPublishedNodeId(mergedNodeId)) {
                if (!containsNodeProperty(retained, property.getPropertyKey(), property.getValue())) {
                    nodePropertyRepository.insert(new GraphPublishedNodeProperty(
                            null,
                            retainedNodeId,
                            property.getPropertyKey(),
                            property.getValue(),
                            !hasPreferred(retained, property.getPropertyKey()) && property.isPreferred()));
                }
            }
        }
    }

    private void mergeNodeMaterials(GraphPublishedNodeId retainedNodeId, List<GraphPublishedNodeId> mergedNodeIds) {
        List<GraphPublishedNodeMaterial> retained = nodeMaterialRepository.listByPublishedNodeId(retainedNodeId);
        List<GraphPublishedNodeId> effectiveMergedNodeIds = safeNodeIds(mergedNodeIds);
        for (GraphPublishedNodeId mergedNodeId : effectiveMergedNodeIds) {
            for (GraphPublishedNodeMaterial relation : nodeMaterialRepository.listByPublishedNodeId(mergedNodeId)) {
                if (containsMaterial(retained, relation.getMaterialRef())) {
                    continue;
                }
                nodeMaterialRepository.insert(new GraphPublishedNodeMaterial(
                        retainedNodeId, relation.getMaterialRef(), relation.getSourceSnapshotJson()));
            }
        }
        nodeMaterialRepository.deleteByPublishedNodeIds(effectiveMergedNodeIds);
    }

    private void mergeEdgeProperties(GraphPublishedEdgeId retainedEdgeId, GraphPublishedEdgeId mergedEdgeId) {
        List<GraphPublishedEdgeProperty> retained = edgePropertyRepository.listByPublishedEdgeId(retainedEdgeId);
        for (GraphPublishedEdgeProperty property : edgePropertyRepository.listByPublishedEdgeId(mergedEdgeId)) {
            if (!containsEdgeProperty(retained, property.getPropertyKey(), property.getValue())) {
                edgePropertyRepository.insert(new GraphPublishedEdgeProperty(
                        null,
                        retainedEdgeId,
                        property.getPropertyKey(),
                        property.getValue(),
                        !hasPreferredEdge(retained, property.getPropertyKey()) && property.isPreferred()));
            }
        }
    }

    private void mergeEdgeMaterials(GraphPublishedEdgeId retainedEdgeId, GraphPublishedEdgeId mergedEdgeId) {
        List<GraphPublishedEdgeMaterial> retained = edgeMaterialRepository.listByPublishedEdgeId(retainedEdgeId);
        for (GraphPublishedEdgeMaterial relation : edgeMaterialRepository.listByPublishedEdgeId(mergedEdgeId)) {
            if (containsEdgeMaterial(retained, relation.getMaterialRef())) {
                continue;
            }
            edgeMaterialRepository.insert(new GraphPublishedEdgeMaterial(
                    retainedEdgeId, relation.getMaterialRef(), relation.getSourceSnapshotJson()));
        }
    }

    private void replaceNodeProperties(GraphPublishedNodeId nodeId, List<GraphPublishedNodeProperty> properties) {
        nodePropertyRepository.deleteByPublishedNodeId(nodeId);
        nodePropertyRepository.batchInsert(safeNodeProperties(properties).stream()
                .peek(property -> {
                    property.setId(null);
                    property.setPublishedNodeId(nodeId);
                })
                .toList());
    }

    private void replaceEdgeProperties(GraphPublishedEdgeId edgeId, List<GraphPublishedEdgeProperty> properties) {
        edgePropertyRepository.deleteByPublishedEdgeId(edgeId);
        edgePropertyRepository.batchInsert(safeEdgeProperties(properties).stream()
                .peek(property -> {
                    property.setId(null);
                    property.setPublishedEdgeId(edgeId);
                })
                .toList());
    }

    private GraphGovernanceImpactResult toImpact(
            GraphPublishedMutationSet changes,
            List<GraphPublishedNodeMaterial> nodeMaterials,
            List<GraphPublishedEdgeMaterial> edgeMaterials,
            List<GraphValidationIssueResult> issues,
            boolean executable) {
        List<GraphPublishedNode> nodes = new ArrayList<>();
        nodes.addAll(changes.createdNodes());
        nodes.addAll(changes.updatedNodes());
        return new GraphGovernanceImpactResult(
                null, nodes, changes.updatedEdges(), nodeMaterials, edgeMaterials, issues, executable);
    }

    private GraphGovernanceImpactResult failedImpact(RuntimeException ex) {
        return new GraphGovernanceImpactResult(
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new GraphValidationIssueResult(
                        ISSUE_GOVERNANCE_FAILED, SEVERITY_BLOCKING, "PUBLISHED_GRAPH", null, null, ex.getMessage())),
                false);
    }

    private GraphGovernanceImpactResult withImpactToken(String operationType, GraphGovernanceImpactResult impact) {
        if (!impact.executable()) {
            return impact;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        impactTokenRepository.insert(new GraphGovernanceImpactToken(
                token, operationType, impactSnapshotJson(impact), now().plus(IMPACT_TOKEN_TTL), null));
        return new GraphGovernanceImpactResult(
                token,
                impact.nodes(),
                impact.edges(),
                impact.nodeMaterials(),
                impact.edgeMaterials(),
                impact.issues(),
                impact.executable());
    }

    private void validateImpactToken(
            String tokenValue, String operationType, GraphGovernanceImpactResult currentImpact) {
        GraphGovernanceImpactToken token = impactTokenRepository.getByToken(tokenValue);
        if (token == null || !operationType.equals(token.getOperationType()) || !token.consumableAt(now())) {
            throw GraphGovernanceImpactToken.stale();
        }
        try {
            JsonNode expected = OBJECT_MAPPER.readTree(token.getSnapshotJson());
            JsonNode actual = OBJECT_MAPPER.readTree(impactSnapshotJson(currentImpact));
            if (!expected.equals(actual)) {
                throw GraphGovernanceImpactToken.stale();
            }
        } catch (JsonProcessingException exception) {
            throw GraphGovernanceImpactToken.stale();
        }
    }

    private String impactSnapshotJson(GraphGovernanceImpactResult impact) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put(
                "nodes",
                impact.nodes().stream()
                        .sorted(Comparator.comparing(node -> node.getId().value()))
                        .map(this::impactNodeSnapshot)
                        .toList());
        snapshot.put(
                "edges",
                impact.edges().stream()
                        .sorted(Comparator.comparing(edge -> edge.getId().value()))
                        .map(this::impactEdgeSnapshot)
                        .toList());
        snapshot.put(
                "nodeMaterials",
                impact.nodeMaterials().stream()
                        .sorted(Comparator.comparing((GraphPublishedNodeMaterial material) ->
                                        material.getPublishedNodeId().value())
                                .thenComparing(
                                        material -> material.getMaterialRef().getContentType())
                                .thenComparing(
                                        material -> material.getMaterialRef().getContentId()))
                        .map(this::impactNodeMaterialSnapshot)
                        .toList());
        snapshot.put(
                "edgeMaterials",
                impact.edgeMaterials().stream()
                        .sorted(Comparator.comparing((GraphPublishedEdgeMaterial material) ->
                                        material.getPublishedEdgeId().value())
                                .thenComparing(
                                        material -> material.getMaterialRef().getContentType())
                                .thenComparing(
                                        material -> material.getMaterialRef().getContentId()))
                        .map(this::impactEdgeMaterialSnapshot)
                        .toList());
        return toJson(snapshot);
    }

    private Map<String, Object> impactNodeSnapshot(GraphPublishedNode node) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", node.getId().value());
        snapshot.put("lockVersion", node.getLockVersion());
        snapshot.put(
                "status", node.getStatus() == null ? null : node.getStatus().name());
        return snapshot;
    }

    private Map<String, Object> impactEdgeSnapshot(GraphPublishedEdge edge) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", edge.getId().value());
        snapshot.put("lockVersion", edge.getLockVersion());
        snapshot.put("sourceNodeId", edge.getSourceNodeId().value());
        snapshot.put("targetNodeId", edge.getTargetNodeId().value());
        snapshot.put(
                "status", edge.getStatus() == null ? null : edge.getStatus().name());
        return snapshot;
    }

    private Map<String, Object> impactNodeMaterialSnapshot(GraphPublishedNodeMaterial material) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("publishedNodeId", material.getPublishedNodeId().value());
        snapshot.put("contentType", material.getMaterialRef().getContentType());
        snapshot.put("contentRefId", material.getMaterialRef().getContentId());
        snapshot.put("sourceSnapshotJson", material.getSourceSnapshotJson());
        return snapshot;
    }

    private Map<String, Object> impactEdgeMaterialSnapshot(GraphPublishedEdgeMaterial material) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("publishedEdgeId", material.getPublishedEdgeId().value());
        snapshot.put("contentType", material.getMaterialRef().getContentType());
        snapshot.put("contentRefId", material.getMaterialRef().getContentId());
        snapshot.put("sourceSnapshotJson", material.getSourceSnapshotJson());
        return snapshot;
    }

    private List<GraphPublishedNodeMaterial> mergeNodeMaterialSnapshot(
            GraphPublishedNodeId retainedNodeId, List<GraphPublishedNodeId> mergedNodeIds) {
        List<GraphPublishedNodeMaterial> result = new ArrayList<>();
        result.addAll(nodeMaterialRepository.listByPublishedNodeId(retainedNodeId));
        for (GraphPublishedNodeId nodeId : safeNodeIds(mergedNodeIds)) {
            result.addAll(nodeMaterialRepository.listByPublishedNodeId(nodeId));
        }
        return result;
    }

    private List<GraphPublishedEdgeMaterial> mergeEdgeMaterials(List<GraphPublishedEdge> edges) {
        return edgeMaterials(edges);
    }

    private List<GraphPublishedNodeMaterial> nodeMaterials(List<GraphPublishedNodeId> nodeIds) {
        List<GraphPublishedNodeMaterial> materials = new ArrayList<>();
        for (GraphPublishedNodeId nodeId : nodeIds == null ? List.<GraphPublishedNodeId>of() : nodeIds) {
            materials.addAll(nodeMaterialRepository.listByPublishedNodeId(nodeId));
        }
        return materials;
    }

    private List<GraphPublishedEdgeMaterial> edgeMaterials(List<GraphPublishedEdge> edges) {
        List<GraphPublishedEdgeMaterial> materials = new ArrayList<>();
        for (GraphPublishedEdge edge : edges == null ? List.<GraphPublishedEdge>of() : edges) {
            materials.addAll(edgeMaterialRepository.listByPublishedEdgeId(edge.getId()));
        }
        return materials;
    }

    private GraphPublishedNodeDetailResult toNodeDetail(GraphPublishedNode node) {
        return new GraphPublishedNodeDetailResult(
                node,
                nodePropertyRepository.listByPublishedNodeId(node.getId()),
                nodeMaterialRepository.listByPublishedNodeId(node.getId()),
                edgeRepository.listByNodeIds(List.of(node.getId())),
                listOperations(TARGET_NODE, node.getId().value()));
    }

    private GraphPublishedEdgeDetailResult toEdgeDetail(GraphPublishedEdge edge) {
        return new GraphPublishedEdgeDetailResult(
                edge,
                nodeRepository.getById(edge.getSourceNodeId()),
                nodeRepository.getById(edge.getTargetNodeId()),
                edgePropertyRepository.listByPublishedEdgeId(edge.getId()),
                edgeMaterialRepository.listByPublishedEdgeId(edge.getId()),
                listOperations(TARGET_EDGE, edge.getId().value()));
    }

    private void recordGovernanceOperation(
            String operationType,
            String targetType,
            Long targetId,
            String reason,
            String beforeSnapshotJson,
            String afterSnapshotJson) {
        Instant operatedAt = now();
        Long auditLogId = auditFacade.record(new SystemAuditFacadeRequest(
                AUDIT_OBJECT_PREFIX + targetType,
                String.valueOf(targetId),
                operationType,
                targetType + ":" + targetId + ":" + operationType + ":" + operatedAt.toEpochMilli(),
                "SYSTEM",
                "GRAPH_GOVERNANCE",
                "图谱治理",
                AUDIT_SOURCE,
                null,
                null,
                null,
                reason,
                beforeSnapshotJson,
                afterSnapshotJson,
                true));
        governanceOperationRepository.insert(new GraphGovernanceOperation(
                null,
                operationType,
                targetType,
                targetId,
                beforeSnapshotJson,
                afterSnapshotJson,
                reason,
                auditLogId,
                operatedAt));
        manualSourceRepository.insert(
                new GraphManualSource(null, targetType, targetId, reason, auditLogId, operatedAt));
    }

    private List<GraphGovernanceOperationResult> listOperations(String targetType, Long targetId) {
        List<GraphGovernanceOperation> operations = governanceOperationRepository.listByTarget(targetType, targetId);
        return (operations == null ? List.<GraphGovernanceOperation>of() : operations)
                .stream().map(this::toOperationResult).toList();
    }

    private GraphGovernanceOperationResult toOperationResult(GraphGovernanceOperation operation) {
        SystemAuditFacadeResponse audit =
                operation.getAuditLogId() == null ? null : auditFacade.get(operation.getAuditLogId());
        return new GraphGovernanceOperationResult(
                operation.getId() == null ? null : operation.getId().value(),
                operation.getOperationType(),
                operation.getTargetType(),
                operation.getTargetId(),
                operation.getReason(),
                operation.getAuditLogId(),
                audit == null ? null : audit.operatorId(),
                audit == null ? null : audit.operatorName(),
                audit == null ? operation.getOperatedAt() : audit.occurredAt(),
                operation.getBeforeSnapshotJson(),
                operation.getAfterSnapshotJson());
    }

    private String nodeSnapshot(GraphPublishedNode node, List<GraphPublishedNodeProperty> properties) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", node.getId() == null ? null : node.getId().value());
        snapshot.put(
                "nodeType",
                node.getNodeType() == null ? null : node.getNodeType().name());
        snapshot.put("name", node.getName());
        snapshot.put(
                "source", node.getSource() == null ? null : node.getSource().name());
        snapshot.put(
                "status", node.getStatus() == null ? null : node.getStatus().name());
        snapshot.put(
                "properties",
                safeNodeProperties(properties).stream()
                        .map(this::nodePropertySnapshot)
                        .toList());
        return toJson(snapshot);
    }

    private String edgeSnapshot(GraphPublishedEdge edge, List<GraphPublishedEdgeProperty> properties) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", edge.getId() == null ? null : edge.getId().value());
        snapshot.put(
                "sourceNodeId",
                edge.getSourceNodeId() == null ? null : edge.getSourceNodeId().value());
        snapshot.put(
                "targetNodeId",
                edge.getTargetNodeId() == null ? null : edge.getTargetNodeId().value());
        snapshot.put("relationType", edge.getRelationType());
        snapshot.put(
                "source", edge.getSource() == null ? null : edge.getSource().name());
        snapshot.put(
                "status", edge.getStatus() == null ? null : edge.getStatus().name());
        snapshot.put("qualifiersJson", edge.getQualifiersJson());
        snapshot.put(
                "properties",
                safeEdgeProperties(properties).stream()
                        .map(this::edgePropertySnapshot)
                        .toList());
        return toJson(snapshot);
    }

    private Map<String, Object> nodePropertySnapshot(GraphPublishedNodeProperty property) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("propertyName", property.getPropertyKey());
        snapshot.put("value", property.getValue());
        snapshot.put("preferred", property.isPreferred());
        return snapshot;
    }

    private Map<String, Object> edgePropertySnapshot(GraphPublishedEdgeProperty property) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("propertyName", property.getPropertyKey());
        snapshot.put("value", property.getValue());
        snapshot.put("preferred", property.isPreferred());
        return snapshot;
    }

    private String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "GRAPH-APP-00001",
                    "knowledge.graph.governance-audit-snapshot-invalid",
                    "Graph governance audit snapshot is invalid",
                    exception);
        }
    }

    private void updateNodesCas(List<GraphPublishedNode> nodes) {
        updateNodesCas(nodes, Map.of());
    }

    private void updateNodesCas(List<GraphPublishedNode> nodes, Map<GraphPublishedNodeId, Long> expectedVersions) {
        for (GraphPublishedNode node : nodes == null ? List.<GraphPublishedNode>of() : nodes) {
            long expectedVersion = expectedVersions.getOrDefault(node.getId(), node.getLockVersion());
            updateNodeCas(node, expectedVersion);
        }
    }

    private void updateEdgesCas(List<GraphPublishedEdge> edges) {
        updateEdgesCas(edges, Map.of());
    }

    private void updateEdgesCas(List<GraphPublishedEdge> edges, Map<GraphPublishedEdgeId, Long> expectedVersions) {
        for (GraphPublishedEdge edge : edges == null ? List.<GraphPublishedEdge>of() : edges) {
            long expectedVersion = expectedVersions.getOrDefault(edge.getId(), edge.getLockVersion());
            updateEdgeCas(edge, expectedVersion);
        }
    }

    private void updateNodeCas(GraphPublishedNode node, long expectedLockVersion) {
        if (nodeRepository.updateIfLockVersion(node, expectedLockVersion) != 1) {
            throw new BizException("Graph published node lock version mismatch");
        }
    }

    private void updateEdgeCas(GraphPublishedEdge edge, long expectedLockVersion) {
        if (edgeRepository.updateIfLockVersion(edge, expectedLockVersion) != 1) {
            throw new BizException("Graph published edge lock version mismatch");
        }
    }

    private void refreshNodeKey(GraphPublishedNode node) {
        node.refreshNodeKey(null);
    }

    private void refreshEdgeKey(GraphPublishedEdge edge) {
        GraphPublishedNode sourceNode = requireNode(edge.getSourceNodeId());
        GraphPublishedNode targetNode = requireNode(edge.getTargetNodeId());
        edge.refreshEdgeKey(
                sourceNode.getNodeKey(),
                targetNode.getNodeKey(),
                schemaSupport.directed(edge.getRelationType()),
                schemaSupport.keyQualifiers(edge.getRelationType(), edge.getQualifiersJson()));
    }

    private void requireNoEdgeKeyConflicts(List<GraphPublishedEdge> edges) {
        Set<GraphEdgeKey> seenKeys = new LinkedHashSet<>();
        for (GraphPublishedEdge edge : edges == null ? List.<GraphPublishedEdge>of() : edges) {
            if (edge.getEdgeKey() == null || !seenKeys.add(edge.getEdgeKey())) {
                throw new BizException("Graph published edge key conflicts after node split");
            }
            GraphPublishedEdge existing = edgeRepository.getByEdgeKey(edge.getEdgeKey());
            if (existing != null && !existing.getId().equals(edge.getId())) {
                throw new BizException("Graph published edge key conflicts after node split");
            }
        }
    }

    private GraphPublishedNode requireCommandNode(GraphPublishedNodeCommand command) {
        if (command == null || command.node() == null) {
            throw new BizException("Graph published node command is required");
        }
        return command.node();
    }

    private GraphPublishedEdge requireCommandEdge(GraphPublishedEdgeCommand command) {
        if (command == null || command.edge() == null) {
            throw new BizException("Graph published edge command is required");
        }
        return command.edge();
    }

    private GraphPublishedNode requireNode(GraphPublishedNodeId nodeId) {
        GraphPublishedNode node = nodeRepository.getById(nodeId);
        if (node == null) {
            throw new BizException("Graph published node does not exist");
        }
        return node;
    }

    private GraphPublishedEdge requireEdge(GraphPublishedEdgeId edgeId) {
        GraphPublishedEdge edge = edgeRepository.getById(edgeId);
        if (edge == null) {
            throw new BizException("Graph published edge does not exist");
        }
        return edge;
    }

    private GraphPublishedNodeProperty requireNodeProperty(GraphPublishedNodePropertyId propertyId) {
        GraphPublishedNodeProperty property = nodePropertyRepository.getById(propertyId);
        if (property == null) {
            throw new BizException("Graph published node property does not exist");
        }
        return property;
    }

    private GraphPublishedNodeMaterial requireNodeMaterial(
            List<GraphPublishedNodeMaterial> materials, ContentRef materialRef) {
        return materials.stream()
                .filter(material -> materialRef != null && materialRef.equals(material.getMaterialRef()))
                .findFirst()
                .orElseThrow(() -> new BizException("Graph published node material mapping does not exist"));
    }

    private boolean containsNodeProperty(List<GraphPublishedNodeProperty> properties, String key, String value) {
        return properties.stream()
                .anyMatch(property -> key.equals(property.getPropertyKey()) && value.equals(property.getValue()));
    }

    private boolean containsEdgeProperty(List<GraphPublishedEdgeProperty> properties, String key, String value) {
        return properties.stream()
                .anyMatch(property -> key.equals(property.getPropertyKey()) && value.equals(property.getValue()));
    }

    private boolean hasPreferred(List<GraphPublishedNodeProperty> properties, String key) {
        return properties.stream()
                .anyMatch(property -> key.equals(property.getPropertyKey()) && property.isPreferred());
    }

    private boolean hasPreferredEdge(List<GraphPublishedEdgeProperty> properties, String key) {
        return properties.stream()
                .anyMatch(property -> key.equals(property.getPropertyKey()) && property.isPreferred());
    }

    private boolean containsMaterial(List<GraphPublishedNodeMaterial> materials, ContentRef materialRef) {
        return materials.stream().anyMatch(material -> materialRef.equals(material.getMaterialRef()));
    }

    private boolean containsEdgeMaterial(List<GraphPublishedEdgeMaterial> materials, ContentRef materialRef) {
        return materials.stream().anyMatch(material -> materialRef.equals(material.getMaterialRef()));
    }

    private List<GraphPublishedNodeId> safeNodeIds(List<GraphPublishedNodeId> nodeIds) {
        return nodeIds == null ? List.of() : nodeIds;
    }

    private List<GraphPublishedNodePropertyId> safePropertyIds(List<GraphPublishedNodePropertyId> propertyIds) {
        return propertyIds == null ? List.of() : propertyIds;
    }

    private List<ContentRef> safeMaterialRefs(List<ContentRef> materialRefs) {
        return materialRefs == null ? List.of() : materialRefs;
    }

    private List<GraphPublishedNodeProperty> safeNodeProperties(List<GraphPublishedNodeProperty> properties) {
        return properties == null ? List.of() : properties;
    }

    private List<GraphPublishedEdgeProperty> safeEdgeProperties(List<GraphPublishedEdgeProperty> properties) {
        return properties == null ? List.of() : properties;
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
