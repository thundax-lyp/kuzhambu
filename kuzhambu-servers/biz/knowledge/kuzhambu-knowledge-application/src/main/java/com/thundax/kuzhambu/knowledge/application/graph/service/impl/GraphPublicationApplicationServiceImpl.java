package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphPublicationExecutor;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublicationApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.operation.GraphPublication;
import com.thundax.kuzhambu.knowledge.domain.graph.model.operation.GraphPublicationContext;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GraphPublicationApplicationServiceImpl implements GraphPublicationApplicationService {

    private final GraphMaterialGraphLoader graphLoader;
    private final GraphPublicationExecutor publicationExecutor;
    private final GraphPublishedNodeRepository publishedNodeRepository;
    private final GraphPublishedEdgeRepository publishedEdgeRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;

    public GraphPublicationApplicationServiceImpl(
            GraphMaterialGraphLoader graphLoader,
            GraphPublicationExecutor publicationExecutor,
            GraphPublishedNodeRepository publishedNodeRepository,
            GraphPublishedEdgeRepository publishedEdgeRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository) {
        this.graphLoader = graphLoader;
        this.publicationExecutor = publicationExecutor;
        this.publishedNodeRepository = publishedNodeRepository;
        this.publishedEdgeRepository = publishedEdgeRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
    }

    @Override
    public GraphPublicationPreviewResult previewPublication(GraphPublicationPreviewQuery query) {
        return previewPublication(requireMaterialRef(query == null ? null : query.materialRef()));
    }

    @Override
    public List<GraphPublicationPreviewResult> previewBatchPublication(GraphBatchPublicationPreviewQuery query) {
        return safeRefs(query == null ? null : query.materialRefs()).stream()
                .map(this::previewPublication)
                .toList();
    }

    @Override
    public GraphPublicationResult publish(GraphPublicationCommand command) {
        return publicationExecutor.publishOne(command);
    }

    @Override
    public List<GraphPublicationResult> publishBatch(GraphBatchPublicationCommand command) {
        return safeCommands(command == null ? null : command.materials()).stream()
                .map(this::publishOneSafely)
                .toList();
    }

    @Override
    public GraphWithdrawalPreviewResult previewWithdrawal(GraphWithdrawalPreviewQuery query) {
        ContentRef materialRef = requireMaterialRef(query == null ? null : query.materialRef());
        List<GraphPublishedNodeMaterial> nodeMaterials = nodeMaterialRepository.listByMaterial(materialRef);
        List<GraphPublishedEdgeMaterial> edgeMaterials = edgeMaterialRepository.listByMaterial(materialRef);
        List<GraphPublishedNode> nodes = publishedNodeRepository.listByIds(nodeMaterials.stream()
                .map(GraphPublishedNodeMaterial::getPublishedNodeId)
                .toList());
        List<GraphPublishedEdge> edges = edgeMaterials.stream()
                .map(GraphPublishedEdgeMaterial::getPublishedEdgeId)
                .map(publishedEdgeRepository::getById)
                .filter(edge -> edge != null)
                .toList();
        return new GraphWithdrawalPreviewResult(materialRef, nodeMaterials.size(), edgeMaterials.size(), nodes, edges);
    }

    @Override
    public GraphMaterial withdraw(GraphWithdrawalCommand command) {
        return publicationExecutor.withdrawOne(command);
    }

    private GraphPublicationPreviewResult previewPublication(ContentRef materialRef) {
        GraphMaterialGraph graph = graphLoader.require(materialRef);
        Map<GraphMaterialNodeId, GraphPublishedNode> matchedNodes = new LinkedHashMap<>();
        graph.nodes().forEach(node -> {
            GraphPublishedNode publishedNode = publishedNodeRepository.getByNodeKey(node.getNodeKey());
            if (publishedNode != null) {
                matchedNodes.put(node.getId(), publishedNode);
            }
        });
        Map<GraphMaterialEdgeId, GraphPublishedEdge> matchedEdges = new LinkedHashMap<>();
        graph.edges().forEach(edge -> {
            GraphPublishedEdge publishedEdge = publishedEdgeRepository.getByEdgeKey(edge.getEdgeKey());
            if (publishedEdge != null) {
                matchedEdges.put(edge.getId(), publishedEdge);
            }
        });
        GraphPublication publication = GraphPublication.plan(
                new GraphPublicationContext(graph, matchedNodes, matchedEdges, Map.of(), Map.of(), Instant.now()));
        return GraphApplicationAssembler.toPublicationPreview(publication);
    }

    private GraphPublicationResult publishOneSafely(GraphPublicationCommand command) {
        try {
            return publicationExecutor.publishOne(command);
        } catch (RuntimeException ex) {
            ContentRef materialRef = command == null ? null : command.materialRef();
            return new GraphPublicationResult(
                    materialRef,
                    materialStatus(materialRef),
                    false,
                    ex.getMessage(),
                    0,
                    0,
                    0,
                    0,
                    List.of(new GraphValidationIssueResult(
                            "GRAPH_PUBLICATION_FAILED", "BLOCKING", "MATERIAL", null, null, ex.getMessage())));
        }
    }

    private GraphMaterialStatus materialStatus(ContentRef materialRef) {
        GraphMaterialStatus status = null;
        if (materialRef == null) {
            return status;
        }
        try {
            status = graphLoader.require(materialRef).material().getStatus();
        } catch (RuntimeException ex) {
            status = null;
        }
        return status;
    }

    private ContentRef requireMaterialRef(ContentRef materialRef) {
        if (materialRef == null) {
            throw new BizException("Graph publication material ref is required");
        }
        return materialRef;
    }

    private List<ContentRef> safeRefs(List<ContentRef> refs) {
        return refs == null ? List.of() : refs;
    }

    private List<GraphPublicationCommand> safeCommands(List<GraphPublicationCommand> commands) {
        return commands == null ? List.of() : commands;
    }
}
