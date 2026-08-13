package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedGraphResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPortalApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GraphPortalApplicationServiceImpl implements GraphPortalApplicationService {

    private final GraphMaterialRepository materialRepository;
    private final GraphPublishedNodeRepository nodeRepository;
    private final GraphPublishedEdgeRepository edgeRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;
    private final GraphMaterialContentResolver contentResolver;

    public GraphPortalApplicationServiceImpl(
            GraphMaterialRepository materialRepository,
            GraphPublishedNodeRepository nodeRepository,
            GraphPublishedEdgeRepository edgeRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository,
            GraphMaterialContentResolver contentResolver) {
        this.materialRepository = materialRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
        this.contentResolver = contentResolver;
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
