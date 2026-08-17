package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.KnowledgeGraphMaterialPageFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.KnowledgeGraphMaterialSnapshotFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.KnowledgeGraphMaterialPageFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.KnowledgeGraphMaterialSnapshotFacadeResponse;
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
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphMaterialContentSnapshotDto;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphDocumentMerger;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphSaver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialStatsRefresher;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSnapshotResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialImportQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialListQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeMergeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialNodeSplitQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialChangeImpactResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialImportPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialPageResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialSourceResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialStatsRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraphMaterialApplicationServiceImpl implements GraphMaterialApplicationService {

    private final GraphMaterialRepository materialRepository;
    private final ClassicsFacade classicsFacade;
    private final GraphMaterialStatsRepository materialStatsRepository;
    private final GraphExtractionTaskRepository extractionTaskRepository;
    private final GraphMaterialNodeRepository nodeRepository;
    private final GraphMaterialEdgeRepository edgeRepository;
    private final GraphMaterialContentResolver contentResolver;
    private final GraphMaterialGraphLoader graphLoader;
    private final GraphMaterialGraphSaver graphSaver;
    private final GraphMaterialStatsRefresher statsRefresher;
    private final GraphSnapshotResolver snapshotSupport;
    private final GraphSchemaResolver schemaSupport;
    private final GraphDocumentMerger documentMerger;

    public GraphMaterialApplicationServiceImpl(
            GraphMaterialRepository materialRepository,
            ClassicsFacade classicsFacade,
            GraphMaterialStatsRepository materialStatsRepository,
            GraphExtractionTaskRepository extractionTaskRepository,
            GraphMaterialNodeRepository nodeRepository,
            GraphMaterialEdgeRepository edgeRepository,
            GraphMaterialContentResolver contentResolver,
            GraphMaterialGraphLoader graphLoader,
            GraphMaterialGraphSaver graphSaver,
            GraphMaterialStatsRefresher statsRefresher,
            GraphSnapshotResolver snapshotSupport,
            GraphSchemaResolver schemaSupport,
            GraphDocumentMerger documentMerger) {
        this.materialRepository = materialRepository;
        this.classicsFacade = classicsFacade;
        this.materialStatsRepository = materialStatsRepository;
        this.extractionTaskRepository = extractionTaskRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.contentResolver = contentResolver;
        this.graphLoader = graphLoader;
        this.graphSaver = graphSaver;
        this.statsRefresher = statsRefresher;
        this.snapshotSupport = snapshotSupport;
        this.schemaSupport = schemaSupport;
        this.documentMerger = documentMerger;
    }

    @Override
    public PageResult<GraphMaterialPageResult> pageMaterials(GraphMaterialListQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        var page = classicsFacade.pageKnowledgeGraphMaterials(KnowledgeGraphMaterialPageFacadeRequest.builder()
                .subjectId(query == null ? null : query.subjectId())
                .keyword(query == null ? null : query.keyword())
                .contentType(query == null ? null : query.contentType())
                .categoryCode(query == null ? null : query.categoryCode())
                .volumeCode(query == null ? null : query.volumeCode())
                .pageNo(effectivePage.getPageNo())
                .pageSize(effectivePage.getPageSize())
                .build());
        List<KnowledgeGraphMaterialPageFacadeResponse.Source> sources =
                page.getRecords() == null ? List.of() : page.getRecords();
        List<ContentRef> contentRefs = sources.stream().map(this::toContentRef).toList();
        Map<ContentRef, GraphMaterial> materialByRef = materialRepository.listByContentRefs(contentRefs).stream()
                .collect(Collectors.toMap(GraphMaterial::getContentRef, Function.identity()));
        List<Long> materialIds = contentRefs.stream()
                .map(materialByRef::get)
                .filter(java.util.Objects::nonNull)
                .map(GraphMaterial::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        Map<Long, GraphMaterialStats> statsByMaterialId =
                materialStatsRepository.listByMaterialIds(materialIds).stream()
                        .collect(Collectors.toMap(GraphMaterialStats::getMaterialId, Function.identity()));
        Map<Long, GraphExtractionTask> latestTaskByMaterialId =
                extractionTaskRepository.listLatestByMaterialIds(materialIds).stream()
                        .collect(Collectors.toMap(GraphExtractionTask::getMaterialId, Function.identity()));
        return PageResult.of(
                page.getPageNo(),
                page.getPageSize(),
                page.getTotalCount(),
                sources.stream()
                        .map(source -> {
                            ContentRef contentRef = toContentRef(source);
                            GraphMaterial material = materialByRef.get(contentRef);
                            Long materialId = material == null ? null : material.getId();
                            return new GraphMaterialPageResult(
                                    toSourceResult(source),
                                    material,
                                    materialId == null ? null : statsByMaterialId.get(materialId),
                                    materialId == null
                                            ? null
                                            : GraphApplicationAssembler.toExtractionTaskResult(
                                                    latestTaskByMaterialId.get(materialId)));
                        })
                        .filter(item -> matchesTaskFilters(
                                item.latestTask(),
                                query == null ? null : query.taskExecutionStatus(),
                                query == null ? null : query.taskDisposition()))
                        .toList());
    }

    private boolean matchesTaskFilters(
            GraphExtractionTaskResult latestTask, String taskExecutionStatus, String taskDisposition) {
        if ((taskExecutionStatus == null || taskExecutionStatus.isBlank())
                && (taskDisposition == null || taskDisposition.isBlank())) {
            return true;
        }
        if (latestTask == null) {
            return false;
        }
        boolean executionMatches = taskExecutionStatus == null
                || taskExecutionStatus.isBlank()
                || taskExecutionStatus.equals(latestTask.executionStatus());
        boolean dispositionMatches = taskDisposition == null
                || taskDisposition.isBlank()
                || taskDisposition.equals(latestTask.disposition());
        return executionMatches && dispositionMatches;
    }

    @Override
    public GraphMaterialResult getMaterialGraph(GraphMaterialQuery query) {
        ContentRef materialRef = requireMaterialRef(query);
        KnowledgeGraphMaterialSnapshotFacadeResponse snapshot =
                classicsFacade.getKnowledgeGraphMaterialSnapshot(KnowledgeGraphMaterialSnapshotFacadeRequest.builder()
                        .subjectId(query.subjectId())
                        .contentType(materialRef.getContentType())
                        .contentId(String.valueOf(materialRef.getContentId()))
                        .build());
        GraphMaterialSourceResult source = toSourceResult(snapshot == null ? null : snapshot.getSource(), null);
        GraphMaterial material = materialRepository.getByContentRef(materialRef);
        GraphMaterialStats stats = material == null ? null : materialStatsRepository.getByMaterialId(material.getId());
        GraphExtractionTask latestTask = material == null
                ? null
                : extractionTaskRepository.listLatestByMaterialIds(List.of(material.getId())).stream()
                        .findFirst()
                        .orElse(null);
        if (material == null) {
            return new GraphMaterialResult(
                    source, null, null, List.of(), List.of(), GraphApplicationAssembler.toExtractionTaskResult(null));
        }
        GraphMaterialGraph graph = GraphMaterialGraph.of(
                material, nodeRepository.listByMaterial(materialRef), edgeRepository.listByMaterial(materialRef));
        return new GraphMaterialResult(
                source,
                graph.material(),
                stats,
                graph.nodes(),
                graph.edges(),
                GraphApplicationAssembler.toExtractionTaskResult(latestTask));
    }

    @Override
    @Transactional
    public GraphMaterialNodeId createNode(GraphMaterialNodeCommand command) {
        GraphMaterialNode node = requireCommandNode(command);
        GraphMaterialGraph graph = graphLoader.require(node.getMaterialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        refreshNodeKey(node);
        graph.addNode(node);
        saveAndRefresh(graph, command.materialLockVersion());
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
        saveAndRefresh(graph, command.materialLockVersion());
    }

    @Override
    @Transactional
    public void deleteNode(GraphMaterialNodeDeleteCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        graph.removeNode(command.nodeId());
        saveAndRefresh(graph, command.materialLockVersion());
    }

    @Override
    @Transactional
    public GraphMaterialEdgeId createEdge(GraphMaterialEdgeCommand command) {
        GraphMaterialEdge edge = requireCommandEdge(command);
        GraphMaterialGraph graph = graphLoader.require(edge.getMaterialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        refreshEdgeKey(graph, edge);
        graph.addEdge(edge);
        saveAndRefresh(graph, command.materialLockVersion());
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
        saveAndRefresh(graph, command.materialLockVersion());
    }

    @Override
    @Transactional
    public void deleteEdge(GraphMaterialEdgeDeleteCommand command) {
        GraphMaterialGraph graph = graphLoader.require(command.materialRef());
        graph.material().requireLockVersion(command.materialLockVersion());
        graph.removeEdge(command.edgeId());
        saveAndRefresh(graph, command.materialLockVersion());
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
        saveAndRefresh(graph, command.materialLockVersion());
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
        saveAndRefresh(graph, command.materialLockVersion());
        return GraphApplicationAssembler.toMaterialResult(graphLoader.require(command.materialRef()));
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
        GraphMaterialGraph saved = graphSaver.replaceDocument(
                graph, documentToImport, GraphSourceType.IMPORT, command.materialLockVersion());
        statsRefresher.refresh(saved.material());
        return GraphApplicationAssembler.toMaterialResult(saved);
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

    private ContentRef toContentRef(KnowledgeGraphMaterialPageFacadeResponse.Source source) {
        if (source == null) {
            throw new BizException("Graph material source is required");
        }
        return new ContentRef(source.getContentType(), Long.valueOf(source.getContentId()));
    }

    private GraphMaterialSourceResult toSourceResult(KnowledgeGraphMaterialPageFacadeResponse.Source source) {
        return toSourceResult(source, null);
    }

    private GraphMaterialSourceResult toSourceResult(
            KnowledgeGraphMaterialPageFacadeResponse.Source source, String summary) {
        if (source == null) {
            return null;
        }
        return new GraphMaterialSourceResult(
                toContentRef(source),
                source.getTitle(),
                summary,
                source.getContentType(),
                source.getCategoryCode(),
                source.getCategoryName(),
                source.getVolumeCode(),
                source.getVolumeName(),
                source.isGraphable());
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
        node.refreshNodeKeyFromFields(schemaSupport.nodeKeyFields(node.getNodeType(), node.getPropertiesJson()));
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

    private void saveAndRefresh(GraphMaterialGraph graph, long expectedLockVersion) {
        graphSaver.save(graph, expectedLockVersion);
        statsRefresher.refresh(graph.material());
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
