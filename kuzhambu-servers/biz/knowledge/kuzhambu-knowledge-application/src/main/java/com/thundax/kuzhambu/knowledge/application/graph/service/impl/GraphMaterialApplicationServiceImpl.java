package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialApplyMode;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialImportCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialVersionRestoreCommand;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphMaterialContentSnapshotDto;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphDocumentMerger;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphSaver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSnapshotResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialImportQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialListQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialChangeImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialImportPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialVersionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraphMaterialApplicationServiceImpl implements GraphMaterialApplicationService {

    private final GraphMaterialRepository materialRepository;
    private final GraphMaterialNodeRepository nodeRepository;
    private final GraphMaterialEdgeRepository edgeRepository;
    private final GraphMaterialVersionRepository versionRepository;
    private final GraphMaterialContentResolver contentResolver;
    private final GraphMaterialGraphLoader graphLoader;
    private final GraphMaterialGraphSaver graphSaver;
    private final GraphSnapshotResolver snapshotSupport;
    private final GraphSchemaResolver schemaSupport;
    private final GraphDocumentMerger documentMerger;

    public GraphMaterialApplicationServiceImpl(
            GraphMaterialRepository materialRepository,
            GraphMaterialNodeRepository nodeRepository,
            GraphMaterialEdgeRepository edgeRepository,
            GraphMaterialVersionRepository versionRepository,
            GraphMaterialContentResolver contentResolver,
            GraphMaterialGraphLoader graphLoader,
            GraphMaterialGraphSaver graphSaver,
            GraphSnapshotResolver snapshotSupport,
            GraphSchemaResolver schemaSupport,
            GraphDocumentMerger documentMerger) {
        this.materialRepository = materialRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.versionRepository = versionRepository;
        this.contentResolver = contentResolver;
        this.graphLoader = graphLoader;
        this.graphSaver = graphSaver;
        this.snapshotSupport = snapshotSupport;
        this.schemaSupport = schemaSupport;
        this.documentMerger = documentMerger;
    }

    @Override
    public PageResult<GraphMaterial> pageMaterials(GraphMaterialListQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        return materialRepository.page(
                query == null ? null : query.keyword(),
                query == null ? null : query.status(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
    }

    @Override
    public GraphMaterialResult getMaterialGraph(GraphMaterialQuery query) {
        return GraphApplicationAssembler.toMaterialResult(graphLoader.require(requireMaterialRef(query)));
    }

    @Override
    @Transactional
    public GraphMaterialNodeId createNode(GraphMaterialNodeCommand command) {
        GraphMaterialNode node = requireCommandNode(command);
        GraphMaterialGraph graph = graphLoader.require(node.getMaterialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        refreshNodeKey(node);
        graph.addNode(node);
        graphSaver.save(graph, command.materialLockVersion());
        return node.getId();
    }

    @Override
    @Transactional
    public void updateNode(GraphMaterialNodeCommand command) {
        GraphMaterialNode node = requireCommandNode(command);
        GraphMaterialGraph graph = graphLoader.require(node.getMaterialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        refreshNodeKey(node);
        graph.updateNode(node);
        refreshAllEdgeKeys(graph);
        graphSaver.save(graph, command.materialLockVersion());
    }

    @Override
    @Transactional
    public void deleteNode(GraphMaterialNodeDeleteCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        graph.removeNode(command.nodeId());
        graphSaver.save(graph, command.materialLockVersion());
    }

    @Override
    @Transactional
    public GraphMaterialEdgeId createEdge(GraphMaterialEdgeCommand command) {
        GraphMaterialEdge edge = requireCommandEdge(command);
        GraphMaterialGraph graph = graphLoader.require(edge.getMaterialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        refreshEdgeKey(graph, edge);
        graph.addEdge(edge);
        graphSaver.save(graph, command.materialLockVersion());
        return edge.getId();
    }

    @Override
    @Transactional
    public void updateEdge(GraphMaterialEdgeCommand command) {
        GraphMaterialEdge edge = requireCommandEdge(command);
        GraphMaterialGraph graph = graphLoader.require(edge.getMaterialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        refreshEdgeKey(graph, edge);
        graph.updateEdge(edge);
        graphSaver.save(graph, command.materialLockVersion());
    }

    @Override
    @Transactional
    public void deleteEdge(GraphMaterialEdgeDeleteCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        graph.removeEdge(command.edgeId());
        graphSaver.save(graph, command.materialLockVersion());
    }

    @Override
    public GraphMaterialChangeImpactResult previewNodeMerge(GraphMaterialNodeMergeQuery query) {
        GraphMaterialGraph graph = graphLoader.require(query.materialRef());
        List<GraphMaterialNodeId> nodeIds = new ArrayList<>();
        nodeIds.add(query.retainedNodeId());
        if (query.mergedNodeIds() != null) {
            nodeIds.addAll(query.mergedNodeIds());
        }
        List<GraphMaterialNode> nodes = graph.nodes().stream()
                .filter(node -> nodeIds.contains(node.getId()))
                .toList();
        List<GraphMaterialEdge> edges = graph.edges().stream()
                .filter(edge -> nodeIds.contains(edge.getSourceNodeId()) || nodeIds.contains(edge.getTargetNodeId()))
                .toList();
        return new GraphMaterialChangeImpactResult(nodes, edges, List.of(), true);
    }

    @Override
    @Transactional
    public GraphMaterialResult mergeNodes(GraphMaterialNodeMergeCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        graph.mergeNodes(command.retainedNodeId(), command.mergedNodeIds());
        refreshAndDeduplicateEdges(graph);
        graphSaver.save(graph, command.materialLockVersion());
        return GraphApplicationAssembler.toMaterialResult(graphLoader.require(command.materialRef()));
    }

    @Override
    public GraphMaterialChangeImpactResult previewNodeSplit(GraphMaterialNodeSplitQuery query) {
        GraphMaterialGraph graph = graphLoader.require(query.materialRef());
        GraphMaterialNode node = requireNode(graph, query.sourceNodeId());
        return new GraphMaterialChangeImpactResult(
                List.of(node), connectedEdges(graph, query.sourceNodeId()), List.of(), true);
    }

    @Override
    @Transactional
    public GraphMaterialResult splitNode(GraphMaterialNodeSplitCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        GraphMaterialNode splitNode = command.splitNode();
        if (splitNode == null || !command.materialRef().equals(splitNode.getMaterialRef())) {
            throw new BizException("Graph material split node does not belong to expected material");
        }
        refreshNodeKey(splitNode);
        splitNode.setId(nodeRepository.insert(splitNode));
        graph.splitNode(command.sourceNodeId(), splitNode, command.reassignedEdgeIds());
        refreshAndDeduplicateEdges(graph);
        graphSaver.save(graph, command.materialLockVersion());
        return GraphApplicationAssembler.toMaterialResult(graphLoader.require(command.materialRef()));
    }

    @Override
    public List<GraphMaterialVersion> listVersions(GraphMaterialQuery query) {
        return versionRepository.listByMaterial(requireMaterialRef(query)).stream()
                .sorted(Comparator.comparingLong(GraphMaterialVersion::getVersionNo)
                        .reversed())
                .toList();
    }

    @Override
    @Transactional
    public GraphMaterialResult restoreVersion(GraphMaterialVersionRestoreCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        graph.material().requireEditable();
        GraphMaterialVersion version =
                versionRepository.getByMaterialAndVersionNo(command.materialRef(), command.versionNo());
        if (version == null) {
            throw new BizException("Graph material version does not exist");
        }
        GraphDocumentDto document = snapshotSupport.parseVersion(version);
        return GraphApplicationAssembler.toMaterialResult(
                graphSaver.replaceDocument(graph, document, GraphSourceType.MATERIAL, command.materialLockVersion()));
    }

    @Override
    public GraphMaterialImportPreviewResult previewImport(GraphMaterialImportQuery query) {
        GraphDocumentDto document = snapshotSupport.parseImport(query.graphJson());
        GraphMaterialGraph graph = previewGraph(query.materialRef());
        List<GraphValidationIssueResult> issues = schemaSupport.validateLoose(document);
        return new GraphMaterialImportPreviewResult(
                GraphApplicationAssembler.toMaterialResult(graph),
                document.getNodes() == null ? 0 : document.getNodes().size(),
                0,
                document.getEdges() == null ? 0 : document.getEdges().size(),
                0,
                issues,
                issues.isEmpty());
    }

    @Override
    @Transactional
    public GraphMaterialResult importGraph(GraphMaterialImportCommand command) {
        GraphDocumentDto document = snapshotSupport.parseImport(command.graphJson());
        if (!schemaSupport.validateLoose(document).isEmpty()) {
            throw new BizException("Graph import does not match graph schema");
        }
        GraphMaterialContentSnapshotDto snapshot = contentResolver.resolveWorkbench(command.materialRef());
        GraphMaterialGraph graph = graphLoader.getOrCreate(command.materialRef(), snapshot.title());
        graph.material().requireLockVersion(command.materialLockVersion());
        GraphDocumentDto documentToImport = documentForMode(graph, document, command.applyMode());
        return GraphApplicationAssembler.toMaterialResult(graphSaver.replaceDocument(
                graph, documentToImport, GraphSourceType.IMPORT, command.materialLockVersion()));
    }

    @Override
    public String exportGraph(GraphMaterialQuery query) {
        return snapshotSupport.serialize(graphLoader.require(requireMaterialRef(query)));
    }

    private GraphMaterialGraph previewGraph(ContentRef materialRef) {
        GraphMaterial material = materialRepository.getByContentRef(materialRef);
        if (material == null) {
            return GraphMaterialGraph.of(
                    new GraphMaterial(materialRef, null, GraphMaterialStatus.DRAFT, null, 0L), List.of(), List.of());
        }
        return GraphMaterialGraph.of(
                material, nodeRepository.listByMaterial(materialRef), edgeRepository.listByMaterial(materialRef));
    }

    private GraphMaterialNode requireCommandNode(GraphMaterialNodeCommand command) {
        if (command == null || command.node() == null || command.node().getMaterialRef() == null) {
            throw new BizException("Graph material node command is required");
        }
        return command.node();
    }

    private GraphMaterialEdge requireCommandEdge(GraphMaterialEdgeCommand command) {
        if (command == null || command.edge() == null || command.edge().getMaterialRef() == null) {
            throw new BizException("Graph material edge command is required");
        }
        return command.edge();
    }

    private ContentRef requireMaterialRef(GraphMaterialQuery query) {
        if (query == null || query.materialRef() == null) {
            throw new BizException("Graph material ref is required");
        }
        return query.materialRef();
    }

    private GraphMaterialNode requireNode(GraphMaterialGraph graph, GraphMaterialNodeId nodeId) {
        return graph.nodes().stream()
                .filter(node -> nodeId != null && nodeId.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new BizException("Graph material node does not exist"));
    }

    private GraphMaterialEdge requireEdge(GraphMaterialGraph graph, GraphMaterialEdgeId edgeId) {
        return graph.edges().stream()
                .filter(edge -> edgeId != null && edgeId.equals(edge.getId()))
                .findFirst()
                .orElseThrow(() -> new BizException("Graph material edge does not exist"));
    }

    private List<GraphMaterialEdge> connectedEdges(GraphMaterialGraph graph, GraphMaterialNodeId nodeId) {
        return graph.edges().stream().filter(edge -> edge.connects(nodeId)).toList();
    }

    private void refreshNodeKey(GraphMaterialNode node) {
        node.refreshNodeKey(schemaSupport.identityQualifier(node.getPropertiesJson()));
    }

    private void refreshEdgeKey(GraphMaterialGraph graph, GraphMaterialEdge edge) {
        GraphMaterialNode sourceNode = requireNode(graph, edge.getSourceNodeId());
        GraphMaterialNode targetNode = requireNode(graph, edge.getTargetNodeId());
        edge.refreshEdgeKey(
                sourceNode,
                targetNode,
                schemaSupport.directed(edge.getRelationType()),
                schemaSupport.keyQualifiers(edge.getRelationType(), edge.getQualifiersJson()));
    }

    private void refreshAndDeduplicateEdges(GraphMaterialGraph graph) {
        refreshAllEdgeKeys(graph);
        graph.deduplicateEdges();
    }

    private void refreshAllEdgeKeys(GraphMaterialGraph graph) {
        graph.edges().forEach(edge -> refreshEdgeKey(graph, edge));
    }

    private GraphDocumentDto documentForMode(
            GraphMaterialGraph graph, GraphDocumentDto document, GraphMaterialApplyMode applyMode) {
        if (applyMode == null) {
            throw new BizException("Graph material apply mode is required");
        }
        return applyMode == GraphMaterialApplyMode.MERGE
                ? documentMerger.merge(snapshotSupport.parseImport(snapshotSupport.serialize(graph)), document)
                : document;
    }
}
