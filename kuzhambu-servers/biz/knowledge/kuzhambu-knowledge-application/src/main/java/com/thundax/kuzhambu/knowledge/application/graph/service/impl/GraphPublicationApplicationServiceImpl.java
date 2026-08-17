package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphPublicationExecutor;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchWithdrawalPreviewItemResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchWithdrawalPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchWithdrawalResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublicationApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublicationPreviewToken;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.operation.GraphPublication;
import com.thundax.kuzhambu.knowledge.domain.graph.model.operation.GraphPublicationContext;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublicationPreviewTokenRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GraphPublicationApplicationServiceImpl implements GraphPublicationApplicationService {

    private static final Duration PREVIEW_TOKEN_TTL = Duration.ofMinutes(15);

    private final ObjectMapper objectMapper;
    private final GraphMaterialGraphLoader graphLoader;
    private final GraphPublicationExecutor publicationExecutor;
    private final GraphPublishedNodeRepository publishedNodeRepository;
    private final GraphPublishedEdgeRepository publishedEdgeRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;
    private final GraphPublicationPreviewTokenRepository previewTokenRepository;
    private final Clock clock;

    public GraphPublicationApplicationServiceImpl(
            ObjectMapper objectMapper,
            GraphMaterialGraphLoader graphLoader,
            GraphPublicationExecutor publicationExecutor,
            GraphPublishedNodeRepository publishedNodeRepository,
            GraphPublishedEdgeRepository publishedEdgeRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository,
            GraphPublicationPreviewTokenRepository previewTokenRepository,
            Clock clock) {
        this.objectMapper = objectMapper;
        this.graphLoader = graphLoader;
        this.publicationExecutor = publicationExecutor;
        this.publishedNodeRepository = publishedNodeRepository;
        this.publishedEdgeRepository = publishedEdgeRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
        this.previewTokenRepository = previewTokenRepository;
        this.clock = clock;
    }

    @Override
    public GraphPublicationPreviewResult previewPublication(GraphPublicationPreviewQuery query) {
        return previewPublication(requireMaterialRef(query == null ? null : query.materialRef()));
    }

    @Override
    public GraphBatchPublicationPreviewResult previewBatchPublication(GraphBatchPublicationPreviewQuery query) {
        return new GraphBatchPublicationPreviewResult(safeRefs(query == null ? null : query.materialRefs()).stream()
                .map(this::previewPublication)
                .toList());
    }

    @Override
    public GraphPublicationResult publish(GraphPublicationCommand command) {
        return publicationExecutor.publishOne(command);
    }

    @Override
    public GraphBatchPublicationResult publishBatch(GraphBatchPublicationCommand command) {
        return new GraphBatchPublicationResult(safeCommands(command == null ? null : command.materials()).stream()
                .map(this::publishOneSafely)
                .toList());
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
    public GraphBatchWithdrawalPreviewResult previewBatchWithdrawal(GraphBatchWithdrawalPreviewQuery query) {
        return new GraphBatchWithdrawalPreviewResult(safeRefs(query == null ? null : query.contentRefs()).stream()
                .map(this::previewWithdrawalSafely)
                .toList());
    }

    @Override
    public GraphMaterial withdraw(GraphWithdrawalCommand command) {
        return publicationExecutor.withdrawOne(command);
    }

    @Override
    public GraphBatchWithdrawalResult withdrawBatch(GraphBatchWithdrawalCommand command) {
        return new GraphBatchWithdrawalResult(
                command == null ? null : command.idempotencyKey(),
                safeWithdrawalCommands(command == null ? null : command.materials()).stream()
                        .map(this::withdrawOneSafely)
                        .toList());
    }

    private GraphPublicationPreviewResult previewPublication(ContentRef materialRef) {
        GraphMaterialGraph graph = graphLoader.require(materialRef);
        Map<GraphMaterialNodeId, GraphPublishedNode> matchedNodes = new LinkedHashMap<>();
        graph.nodes().forEach(node -> {
            if (node.getNodeKey() == null) {
                return;
            }
            GraphPublishedNode publishedNode = publishedNodeRepository.getByNodeKey(node.getNodeKey());
            if (publishedNode != null) {
                matchedNodes.put(node.getId(), publishedNode);
            }
        });
        Map<GraphMaterialEdgeId, GraphPublishedEdge> matchedEdges = new LinkedHashMap<>();
        graph.edges().forEach(edge -> {
            if (edge.getEdgeKey() == null) {
                return;
            }
            GraphPublishedEdge publishedEdge = publishedEdgeRepository.getByEdgeKey(edge.getEdgeKey());
            if (publishedEdge != null) {
                matchedEdges.put(edge.getId(), publishedEdge);
            }
        });
        GraphPublication publication = GraphPublication.plan(
                new GraphPublicationContext(graph, matchedNodes, matchedEdges, Map.of(), Map.of(), now()));
        GraphPublicationPreviewResult preview = GraphApplicationAssembler.toPublicationPreview(publication);
        String previewToken = UUID.randomUUID().toString().replace("-", "");
        previewTokenRepository.insert(new GraphPublicationPreviewToken(
                previewToken,
                materialRef,
                graph.material().getLockVersion(),
                previewSnapshotJson(preview),
                now().plus(PREVIEW_TOKEN_TTL),
                null));
        return new GraphPublicationPreviewResult(
                previewToken,
                preview.materialRef(),
                preview.materialLockVersion(),
                preview.nodes(),
                preview.edges(),
                preview.issues(),
                preview.publishable());
    }

    private String previewSnapshotJson(GraphPublicationPreviewResult preview) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("materialLockVersion", preview.materialLockVersion());
        snapshot.put(
                "nodes",
                preview.nodes().stream()
                        .map(node -> {
                            Map<String, Object> row = new LinkedHashMap<>();
                            row.put(
                                    "materialObjectId",
                                    node.materialNode().getId().value());
                            row.put("matchType", node.matchedNode() == null ? "CREATE" : "CONFLICT");
                            row.put(
                                    "matchedObjectId",
                                    node.matchedNode() == null
                                            ? null
                                            : node.matchedNode().getId().value());
                            row.put(
                                    "matchedObjectLockVersion",
                                    node.matchedNode() == null
                                            ? null
                                            : node.matchedNode().getLockVersion());
                            return row;
                        })
                        .toList());
        snapshot.put(
                "edges",
                preview.edges().stream()
                        .map(edge -> {
                            Map<String, Object> row = new LinkedHashMap<>();
                            row.put(
                                    "materialObjectId",
                                    edge.materialEdge().getId().value());
                            row.put("matchType", edge.matchedEdge() == null ? "CREATE" : "CONFLICT");
                            row.put(
                                    "matchedObjectId",
                                    edge.matchedEdge() == null
                                            ? null
                                            : edge.matchedEdge().getId().value());
                            row.put(
                                    "matchedObjectLockVersion",
                                    edge.matchedEdge() == null
                                            ? null
                                            : edge.matchedEdge().getLockVersion());
                            return row;
                        })
                        .toList());
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new BizException("Graph publication preview snapshot cannot be serialized");
        }
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

    private GraphBatchWithdrawalPreviewItemResult previewWithdrawalSafely(ContentRef materialRef) {
        try {
            return new GraphBatchWithdrawalPreviewItemResult(
                    materialRef, previewWithdrawal(new GraphWithdrawalPreviewQuery(materialRef)), null, null);
        } catch (RuntimeException ex) {
            return new GraphBatchWithdrawalPreviewItemResult(materialRef, null, failureCode(ex), ex.getMessage());
        }
    }

    private GraphWithdrawalResult withdrawOneSafely(GraphWithdrawalCommand command) {
        ContentRef materialRef = command == null ? null : command.materialRef();
        try {
            return new GraphWithdrawalResult(materialRef, true, publicationExecutor.withdrawOne(command), null, null);
        } catch (RuntimeException ex) {
            return new GraphWithdrawalResult(materialRef, false, null, failureCode(ex), ex.getMessage());
        }
    }

    private String failureCode(RuntimeException ex) {
        return ex instanceof BizException bizException && bizException.getCode() != null
                ? bizException.getCode()
                : "GRAPH_WITHDRAWAL_FAILED";
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

    private List<GraphWithdrawalCommand> safeWithdrawalCommands(List<GraphWithdrawalCommand> commands) {
        return commands == null ? List.of() : commands;
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
