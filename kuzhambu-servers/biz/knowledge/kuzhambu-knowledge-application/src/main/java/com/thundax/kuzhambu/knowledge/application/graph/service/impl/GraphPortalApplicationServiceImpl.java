package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphOneHopEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphOneHopEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPortalOverviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedGraphResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphRecentEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPortalApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GraphPortalApplicationServiceImpl implements GraphPortalApplicationService {

    private final GraphMaterialRepository materialRepository;
    private final GraphPublishedNodeRepository nodeRepository;
    private final GraphPublishedEdgeRepository edgeRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;
    private final GraphMaterialContentResolver contentResolver;
    private final GraphWorkbenchApplicationService workbenchService;

    public GraphPortalApplicationServiceImpl(
            GraphMaterialRepository materialRepository,
            GraphPublishedNodeRepository nodeRepository,
            GraphPublishedEdgeRepository edgeRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository,
            GraphMaterialContentResolver contentResolver,
            GraphWorkbenchApplicationService workbenchService) {
        this.materialRepository = materialRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
        this.contentResolver = contentResolver;
        this.workbenchService = workbenchService;
    }

    @Override
    public GraphPublishedGraphResult getMaterialGraph(GraphMaterialQuery query) {
        ContentRef materialRef = requireMaterialRef(query);
        GraphMaterial material = materialRepository.getByContentRef(materialRef);
        if (material == null || material.getStatus() != GraphMaterialStatus.PUBLISHED) {
            return empty(materialRef);
        }
        if (!contentResolver.isPortalVisible(materialRef)) {
            return empty(materialRef);
        }
        List<GraphPublishedNode> nodes = nodeRepository
                .listByIds(nodeMaterialRepository.listByMaterial(materialRef).stream()
                        .map(GraphPublishedNodeMaterial::getPublishedNodeId)
                        .toList())
                .stream()
                .filter(node -> node.getStatus() == GraphPublishedStatus.ACTIVE)
                .toList();
        Set<GraphPublishedNodeId> nodeIds = new LinkedHashSet<>(
                nodes.stream().map(GraphPublishedNode::getId).toList());
        List<GraphPublishedEdge> edges = edgeMaterialRepository.listByMaterial(materialRef).stream()
                .map(GraphPublishedEdgeMaterial::getPublishedEdgeId)
                .map(edgeRepository::getById)
                .filter(edge -> edge != null && edge.getStatus() == GraphPublishedStatus.ACTIVE)
                .filter(edge -> nodeIds.contains(edge.getSourceNodeId()) && nodeIds.contains(edge.getTargetNodeId()))
                .toList();
        return new GraphPublishedGraphResult(materialRef, true, nodes, edges);
    }

    @Override
    public GraphPortalOverviewResult getOverview() {
        List<ContentRef> visibleMaterialRefs =
                materialRepository.listContentRefsByStatus(GraphMaterialStatus.PUBLISHED).stream()
                        .filter(contentResolver::isPortalVisible)
                        .toList();
        Map<ContentRef, List<GraphPublishedNodeId>> nodeIdsByMaterial = new LinkedHashMap<>();
        Map<ContentRef, List<GraphPublishedEdgeId>> edgeIdsByMaterial = new LinkedHashMap<>();
        Set<GraphPublishedNodeId> nodeIds = new LinkedHashSet<>();
        Set<GraphPublishedEdgeId> edgeIds = new LinkedHashSet<>();
        for (ContentRef materialRef : visibleMaterialRefs) {
            List<GraphPublishedNodeId> materialNodeIds = nodeMaterialRepository.listByMaterial(materialRef).stream()
                    .map(GraphPublishedNodeMaterial::getPublishedNodeId)
                    .toList();
            List<GraphPublishedEdgeId> materialEdgeIds = edgeMaterialRepository.listByMaterial(materialRef).stream()
                    .map(GraphPublishedEdgeMaterial::getPublishedEdgeId)
                    .toList();
            nodeIdsByMaterial.put(materialRef, materialNodeIds);
            edgeIdsByMaterial.put(materialRef, materialEdgeIds);
            nodeIds.addAll(materialNodeIds);
            edgeIds.addAll(materialEdgeIds);
        }
        Set<GraphPublishedNodeId> activeNodeIds = nodeRepository.listByIds(List.copyOf(nodeIds)).stream()
                .filter(node -> node != null && node.getStatus() == GraphPublishedStatus.ACTIVE)
                .map(GraphPublishedNode::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<GraphPublishedEdge> activeEdges = edgeIds.stream()
                .map(edgeRepository::getById)
                .filter(edge -> edge != null && edge.getStatus() == GraphPublishedStatus.ACTIVE)
                .filter(edge -> activeNodeIds.contains(edge.getSourceNodeId())
                        && activeNodeIds.contains(edge.getTargetNodeId()))
                .toList();
        Set<GraphPublishedEdgeId> activeEdgeIds =
                activeEdges.stream().map(GraphPublishedEdge::getId).collect(Collectors.toSet());
        Set<GraphPublishedNodeId> connectedNodeIds = new LinkedHashSet<>();
        for (GraphPublishedEdge edge : activeEdges) {
            connectedNodeIds.add(edge.getSourceNodeId());
            connectedNodeIds.add(edge.getTargetNodeId());
        }
        long coveredMaterialCount = visibleMaterialRefs.stream()
                .filter(ref -> nodeIdsByMaterial.get(ref).stream().anyMatch(activeNodeIds::contains)
                        || edgeIdsByMaterial.get(ref).stream().anyMatch(activeEdgeIds::contains))
                .count();
        return new GraphPortalOverviewResult(
                activeNodeIds.size(),
                activeEdges.size(),
                coveredMaterialCount,
                activeNodeIds.stream()
                        .filter(id -> !connectedNodeIds.contains(id))
                        .count());
    }

    @Override
    public GraphRecentEdgesResult listRecentEdges() {
        GraphRecentEdgesResult source = workbenchService.listRecentEdges();
        VisibleGraph graph = visibleGraph(source.nodes(), source.edges());
        return new GraphRecentEdgesResult(graph.nodes(), graph.edges());
    }

    @Override
    public GraphOneHopEdgesResult listOneHopEdges(GraphOneHopEdgesQuery query) {
        GraphOneHopEdgesResult source = workbenchService.listOneHopEdges(query);
        VisibleGraph graph = visibleGraph(source.nodes(), source.edges());
        return new GraphOneHopEdgesResult(graph.nodes(), graph.edges(), source.nextCursor(), source.truncated());
    }

    private VisibleGraph visibleGraph(List<GraphPublishedNode> nodes, List<GraphPublishedEdge> edges) {
        Map<ContentRef, Boolean> materialVisibility = new LinkedHashMap<>();
        Map<GraphPublishedNodeId, GraphPublishedNode> visibleNodesById = new LinkedHashMap<>();
        for (GraphPublishedNode node : nodes == null ? List.<GraphPublishedNode>of() : nodes) {
            if (node != null
                    && node.getStatus() == GraphPublishedStatus.ACTIVE
                    && nodeMaterialRepository.listByPublishedNodeId(node.getId()).stream()
                            .map(GraphPublishedNodeMaterial::getMaterialRef)
                            .anyMatch(ref -> isPortalVisibleMaterial(ref, materialVisibility))) {
                visibleNodesById.put(node.getId(), node);
            }
        }
        List<GraphPublishedEdge> visibleEdges = (edges == null ? List.<GraphPublishedEdge>of() : edges)
                .stream()
                        .filter(edge -> edge != null && edge.getStatus() == GraphPublishedStatus.ACTIVE)
                        .filter(edge -> visibleNodesById.containsKey(edge.getSourceNodeId())
                                && visibleNodesById.containsKey(edge.getTargetNodeId()))
                        .filter(edge -> edgeMaterialRepository.listByPublishedEdgeId(edge.getId()).stream()
                                .map(GraphPublishedEdgeMaterial::getMaterialRef)
                                .anyMatch(ref -> isPortalVisibleMaterial(ref, materialVisibility)))
                        .toList();
        Set<GraphPublishedNodeId> acceptedNodeIds = new LinkedHashSet<>();
        for (GraphPublishedEdge edge : visibleEdges) {
            acceptedNodeIds.add(edge.getSourceNodeId());
            acceptedNodeIds.add(edge.getTargetNodeId());
        }
        return new VisibleGraph(
                acceptedNodeIds.stream().map(visibleNodesById::get).toList(), visibleEdges);
    }

    private boolean isPortalVisibleMaterial(ContentRef ref, Map<ContentRef, Boolean> materialVisibility) {
        return materialVisibility.computeIfAbsent(ref, key -> {
            GraphMaterial material = materialRepository.getByContentRef(key);
            return material != null
                    && material.getStatus() == GraphMaterialStatus.PUBLISHED
                    && contentResolver.isPortalVisible(key);
        });
    }

    private record VisibleGraph(List<GraphPublishedNode> nodes, List<GraphPublishedEdge> edges) {}

    private GraphPublishedGraphResult empty(ContentRef materialRef) {
        return new GraphPublishedGraphResult(materialRef, false, List.of(), List.of());
    }

    private ContentRef requireMaterialRef(GraphMaterialQuery query) {
        if (query == null || query.materialRef() == null) {
            throw new BizException("Graph material ref is required");
        }
        return query.materialRef();
    }
}
