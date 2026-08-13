package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.assembler.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEdgeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialImportCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeMergeCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialNodeSplitCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialVersionRestoreCommand;
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
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphDocument;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphDocumentPlan;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphDocumentPlanner;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphMaterialContentSnapshot;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphMaterialGraphSaver;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphSchemaSupport;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphSnapshotSupport;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialChangeSet;
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
    private final GraphDocumentPlanner documentPlanner;
    private final GraphSnapshotSupport snapshotSupport;
    private final GraphSchemaSupport schemaSupport;

    public GraphMaterialApplicationServiceImpl(
            GraphMaterialRepository materialRepository,
            GraphMaterialNodeRepository nodeRepository,
            GraphMaterialEdgeRepository edgeRepository,
            GraphMaterialVersionRepository versionRepository,
            GraphMaterialContentResolver contentResolver,
            GraphMaterialGraphLoader graphLoader,
            GraphMaterialGraphSaver graphSaver,
            GraphDocumentPlanner documentPlanner,
            GraphSnapshotSupport snapshotSupport,
            GraphSchemaSupport schemaSupport) {
        this.materialRepository = materialRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.versionRepository = versionRepository;
        this.contentResolver = contentResolver;
        this.graphLoader = graphLoader;
        this.graphSaver = graphSaver;
        this.documentPlanner = documentPlanner;
        this.snapshotSupport = snapshotSupport;
        this.schemaSupport = schemaSupport;
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
        GraphMaterialChangeSet changes = GraphMaterialChangeSet.empty();
        changes.addCreatedNode(node);
        graphSaver.save(graph, changes, command.materialLockVersion());
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
        GraphMaterialChangeSet changes = GraphMaterialChangeSet.empty();
        changes.addUpdatedNode(node);
        connectedEdges(graph, node.getId()).forEach(edge -> {
            refreshEdgeKey(graph, edge);
            changes.addUpdatedEdge(edge);
        });
        graphSaver.save(graph, changes, command.materialLockVersion());
    }

    @Override
    @Transactional
    public void deleteNode(GraphMaterialNodeDeleteCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        GraphMaterialNode node = requireNode(graph, command.nodeId());
        List<GraphMaterialEdge> deletedEdges = connectedEdges(graph, command.nodeId());
        graph.removeNode(command.nodeId());
        GraphMaterialChangeSet changes = GraphMaterialChangeSet.empty();
        changes.addDeletedNode(node);
        deletedEdges.forEach(changes::addDeletedEdge);
        graphSaver.save(graph, changes, command.materialLockVersion());
    }

    @Override
    @Transactional
    public GraphMaterialEdgeId createEdge(GraphMaterialEdgeCommand command) {
        GraphMaterialEdge edge = requireCommandEdge(command);
        GraphMaterialGraph graph = graphLoader.require(edge.getMaterialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        refreshEdgeKey(graph, edge);
        graph.addEdge(edge);
        GraphMaterialChangeSet changes = GraphMaterialChangeSet.empty();
        changes.addCreatedEdge(edge);
        graphSaver.save(graph, changes, command.materialLockVersion());
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
        GraphMaterialChangeSet changes = GraphMaterialChangeSet.empty();
        changes.addUpdatedEdge(edge);
        graphSaver.save(graph, changes, command.materialLockVersion());
    }

    @Override
    @Transactional
    public void deleteEdge(GraphMaterialEdgeDeleteCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        GraphMaterialEdge edge = requireEdge(graph, command.edgeId());
        graph.removeEdge(command.edgeId());
        GraphMaterialChangeSet changes = GraphMaterialChangeSet.empty();
        changes.addDeletedEdge(edge);
        graphSaver.save(graph, changes, command.materialLockVersion());
    }

    @Override
    public GraphMaterialChangeImpactResult previewNodeMerge(GraphMaterialNodeMergeQuery query) {
        GraphMaterialGraph graph = graphLoader.require(query.materialRef());
        GraphMaterialChangeSet changes = graph.mergeNodes(query.retainedNodeId(), query.mergedNodeIds());
        return toChangeImpact(changes, List.of(), true);
    }

    @Override
    @Transactional
    public GraphMaterialResult mergeNodes(GraphMaterialNodeMergeCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        GraphMaterialChangeSet changes = graph.mergeNodes(command.retainedNodeId(), command.mergedNodeIds());
        graphSaver.save(graph, changes, command.materialLockVersion());
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
        GraphMaterialChangeSet changes =
                graph.splitNode(command.sourceNodeId(), splitNode, command.reassignedEdgeIds());
        changes.updatedEdges().forEach(edge -> refreshEdgeKey(graph, edge));
        graphSaver.save(graph, changes, command.materialLockVersion());
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
        GraphDocument document = snapshotSupport.parseVersion(version);
        GraphDocumentPlan plan = documentPlanner.plan(graph, document, GraphSourceType.MATERIAL, "REPLACE");
        return GraphApplicationAssembler.toMaterialResult(
                graphSaver.applyDocument(graph, plan, command.materialLockVersion()));
    }

    @Override
    public GraphMaterialImportPreviewResult previewImport(GraphMaterialImportQuery query) {
        GraphDocument document = snapshotSupport.parseImport(query.graphJson());
        GraphMaterialGraph graph = previewGraph(query.materialRef());
        GraphDocumentPlan plan = documentPlanner.plan(graph, document, GraphSourceType.IMPORT, query.strategy());
        List<GraphValidationIssueResult> issues = schemaSupport.validateLoose(document);
        return new GraphMaterialImportPreviewResult(
                GraphApplicationAssembler.toMaterialResult(graph),
                plan.createdNodes().size(),
                plan.updatedNodes().size(),
                plan.createdEdges().size(),
                plan.updatedEdges().size(),
                issues,
                issues.isEmpty());
    }

    @Override
    @Transactional
    public GraphMaterialResult importGraph(GraphMaterialImportCommand command) {
        GraphDocument document = snapshotSupport.parseImport(command.graphJson());
        GraphMaterialContentSnapshot snapshot = contentResolver.resolveWorkbench(command.materialRef());
        GraphMaterialGraph graph = graphLoader.getOrCreate(command.materialRef(), snapshot.title());
        graph.material().requireLockVersion(command.materialLockVersion());
        GraphDocumentPlan plan = documentPlanner.plan(graph, document, GraphSourceType.IMPORT, command.strategy());
        return GraphApplicationAssembler.toMaterialResult(
                graphSaver.applyDocument(graph, plan, command.materialLockVersion()));
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

    private GraphMaterialChangeImpactResult toChangeImpact(
            GraphMaterialChangeSet changes, List<GraphValidationIssueResult> issues, boolean executable) {
        List<GraphMaterialNode> nodes = new ArrayList<>();
        nodes.addAll(changes.createdNodes());
        nodes.addAll(changes.updatedNodes());
        nodes.addAll(changes.deletedNodes());
        List<GraphMaterialEdge> edges = new ArrayList<>();
        edges.addAll(changes.createdEdges());
        edges.addAll(changes.updatedEdges());
        edges.addAll(changes.deletedEdges());
        return new GraphMaterialChangeImpactResult(nodes, edges, issues, executable);
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
}
