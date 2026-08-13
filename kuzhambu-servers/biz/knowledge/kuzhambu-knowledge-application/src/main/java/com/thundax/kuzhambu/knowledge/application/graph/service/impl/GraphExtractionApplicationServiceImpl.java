package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.AiBatchJobQueryFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeGraphExtractionJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobPageFacadeResponse;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionApplyCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphMaterialContentSnapshotDto;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphSaver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSnapshotResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphExtractionQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraphExtractionApplicationServiceImpl implements GraphExtractionApplicationService {

    private static final String AI_SCOPE = "KNOWLEDGE_GRAPH";
    private static final String AI_CAPABILITY = "KNOWLEDGE_GRAPH_EXTRACT";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_FAILED = "FAILED";

    private final AiFacade aiFacade;
    private final ObjectMapper objectMapper;
    private final GraphMaterialContentResolver contentResolver;
    private final GraphMaterialGraphLoader graphLoader;
    private final GraphSnapshotResolver snapshotSupport;
    private final GraphSchemaResolver schemaSupport;
    private final GraphMaterialGraphSaver graphSaver;

    public GraphExtractionApplicationServiceImpl(
            AiFacade aiFacade,
            ObjectMapper objectMapper,
            GraphMaterialContentResolver contentResolver,
            GraphMaterialGraphLoader graphLoader,
            GraphSnapshotResolver snapshotSupport,
            GraphSchemaResolver schemaSupport,
            GraphMaterialGraphSaver graphSaver) {
        this.aiFacade = aiFacade;
        this.objectMapper = objectMapper;
        this.contentResolver = contentResolver;
        this.graphLoader = graphLoader;
        this.snapshotSupport = snapshotSupport;
        this.schemaSupport = schemaSupport;
        this.graphSaver = graphSaver;
    }

    @Override
    public GraphExtractionResult startExtraction(GraphExtractionCommand command) {
        ContentRef materialRef = requireMaterialRef(command == null ? null : command.materialRef());
        GraphMaterialContentSnapshotDto snapshot = contentResolver.resolveWorkbench(materialRef);
        graphLoader.getOrCreate(materialRef, snapshot.title()).material().requireEditable();
        rejectRunningJob(materialRef);
        AiBatchJobActionFacadeResponse action = aiFacade.submitKnowledgeGraphExtraction(
                extractionRequest(materialRef, snapshot, command == null ? null : command.requestedBy()));
        return GraphApplicationAssembler.toExtractionResult(aiFacade.getBatchJob(action.getBatchId()));
    }

    @Override
    public GraphExtractionResult retryExtraction(GraphExtractionRetryCommand command) {
        if (command == null) {
            throw new BizException("Graph extraction retry command is required");
        }
        ContentRef materialRef = requireMaterialRef(command == null ? null : command.materialRef());
        if (command.failedBatchJobId() == null) {
            throw new BizException("Failed graph extraction batch job id is required");
        }
        AiBatchJobFacadeResponse failedJob = aiFacade.getBatchJob(command.failedBatchJobId());
        requireRetryableFailedJob(materialRef, failedJob);
        GraphMaterialContentSnapshotDto snapshot = contentResolver.resolveWorkbench(materialRef);
        graphLoader.getOrCreate(materialRef, snapshot.title()).material().requireEditable();
        rejectRunningJob(materialRef);
        AiBatchJobActionFacadeResponse action = aiFacade.submitKnowledgeGraphExtraction(
                extractionRequest(materialRef, snapshot, command.requestedBy()));
        return GraphApplicationAssembler.toExtractionResult(aiFacade.getBatchJob(action.getBatchId()));
    }

    @Override
    public GraphExtractionResult getCurrentExtraction(GraphExtractionQuery query) {
        return GraphApplicationAssembler.toExtractionResult(
                aiFacade.getLatestBatchJob(queryRequest(requireQueryMaterialRef(query), STATUS_RUNNING, null)));
    }

    @Override
    public PageResult<GraphExtractionResult> pageExtractionHistory(GraphExtractionQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        AiBatchJobPageFacadeResponse page =
                aiFacade.pageBatchJobs(queryRequest(requireQueryMaterialRef(query), null, effectivePage));
        return PageResult.of(
                page.getPageNo(),
                page.getPageSize(),
                page.getTotalCount(),
                page.getRecords().stream()
                        .map(GraphApplicationAssembler::toExtractionResult)
                        .toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public GraphMaterialResult applyExtractionResult(GraphExtractionApplyCommand command) {
        ContentRef materialRef = requireMaterialRef(command == null ? null : command.materialRef());
        if (command.candidateId() == null) {
            throw new BizException("Graph extraction candidate id is required");
        }
        AiCandidateFacadeDto candidate =
                aiFacade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                        .candidateId(command.candidateId())
                        .contentType(ContentRefCodec.toContentType(materialRef))
                        .contentId(ContentRefCodec.toValue(materialRef))
                        .capability(AI_CAPABILITY)
                        .build());
        requireCandidate(materialRef, candidate);
        GraphDocumentDto document = snapshotSupport.parseCandidate(candidate.getResultPayload());
        List<GraphValidationIssueResult> issues = schemaSupport.validateLoose(document);
        if (!issues.isEmpty()) {
            throw new BizException("Graph extraction candidate does not match graph schema");
        }
        GraphMaterialGraph graph = graphLoader.require(materialRef);
        graph.material().requireLockVersion(command.materialLockVersion());
        graph.material().requireEditable();
        GraphMaterialResult result = GraphApplicationAssembler.toMaterialResult(
                graphSaver.replaceDocument(graph, document, GraphSourceType.AI, command.materialLockVersion()));
        aiFacade.markCandidateApplied(MarkAiCandidateAppliedFacadeRequest.builder()
                .candidateId(candidate.getCandidateId())
                .resultFormat(candidate.getResultFormat())
                .resultPayload(candidate.getResultPayload())
                .appliedAt(Instant.now())
                .build());
        return result;
    }

    private KnowledgeGraphExtractionJobFacadeRequest extractionRequest(
            ContentRef materialRef, GraphMaterialContentSnapshotDto snapshot, Long requestedBy) {
        return KnowledgeGraphExtractionJobFacadeRequest.builder()
                .scope(AI_SCOPE)
                .contentType(ContentRefCodec.toContentType(materialRef))
                .contentId(ContentRefCodec.toValue(materialRef))
                .contentTitle(snapshot.title())
                .contentSnapshotJson(snapshotJson(snapshot))
                .requestedBy(requestedBy)
                .build();
    }

    private AiBatchJobQueryFacadeRequest queryRequest(ContentRef materialRef, String status, PageQuery pageQuery) {
        return AiBatchJobQueryFacadeRequest.builder()
                .scope(AI_SCOPE)
                .capability(AI_CAPABILITY)
                .status(status)
                .contentType(ContentRefCodec.toContentType(materialRef))
                .contentId(ContentRefCodec.toValue(materialRef))
                .pageNo(pageQuery == null ? 1 : pageQuery.getPageNo())
                .pageSize(pageQuery == null ? 1 : pageQuery.getPageSize())
                .build();
    }

    private void rejectRunningJob(ContentRef materialRef) {
        AiBatchJobFacadeResponse runningJob =
                aiFacade.getLatestBatchJob(queryRequest(materialRef, STATUS_RUNNING, null));
        if (runningJob != null) {
            throw new BizException("Graph extraction is already running for material");
        }
    }

    private void requireRetryableFailedJob(ContentRef materialRef, AiBatchJobFacadeResponse job) {
        if (job == null
                || !AI_CAPABILITY.equals(job.getCapability())
                || !STATUS_FAILED.equals(job.getStatus())
                || !ContentRefCodec.toContentType(materialRef).equals(job.getContentType())
                || !ContentRefCodec.toValue(materialRef).equals(job.getContentId())) {
            throw new BizException("Graph extraction batch job is not retryable");
        }
    }

    private void requireCandidate(ContentRef materialRef, AiCandidateFacadeDto candidate) {
        if (candidate == null
                || !AI_CAPABILITY.equals(candidate.getCapability())
                || !ContentRefCodec.toContentType(materialRef).equals(candidate.getContentType())
                || !ContentRefCodec.toValue(materialRef).equals(candidate.getContentId())) {
            throw new BizException("Graph extraction candidate does not belong to material");
        }
    }

    private ContentRef requireQueryMaterialRef(GraphExtractionQuery query) {
        return requireMaterialRef(query == null ? null : query.materialRef());
    }

    private ContentRef requireMaterialRef(ContentRef materialRef) {
        if (materialRef == null) {
            throw new BizException("Graph extraction material ref is required");
        }
        return materialRef;
    }

    private String snapshotJson(GraphMaterialContentSnapshotDto snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new BizException("Graph extraction content snapshot cannot be serialized");
        }
    }
}
