package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.KnowledgeGraphExtractionJobFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RejectAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionBatchCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCancelCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCandidateDiscardCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRegenerateCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialApplyMode;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphDocumentDto;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphMaterialContentSnapshotDto;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphDocumentMerger;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphSaver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialStatsRefresher;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSnapshotResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphTaskCandidateResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskDetailQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.support.GraphApplicationAssembler;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.operator.GraphExtractionTaskOperator;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class GraphExtractionApplicationServiceImpl implements GraphExtractionApplicationService {

    private static final String AI_SCOPE = "KNOWLEDGE_GRAPH";
    private static final String AI_CAPABILITY = "KNOWLEDGE_GRAPH_EXTRACT";
    private static final int PURGE_AFTER_DAYS = 7;

    private final AiFacade aiFacade;
    private final ObjectMapper objectMapper;
    private final GraphMaterialContentResolver contentResolver;
    private final GraphMaterialGraphLoader graphLoader;
    private final GraphSnapshotResolver snapshotSupport;
    private final GraphSchemaResolver schemaSupport;
    private final GraphMaterialGraphSaver graphSaver;
    private final GraphMaterialStatsRefresher statsRefresher;
    private final GraphDocumentMerger documentMerger;
    private final GraphMaterialRepository materialRepository;
    private final GraphExtractionTaskRepository taskRepository;
    private final GraphExtractionTaskOperator taskOperator;
    private final GraphTaskCandidateResolver candidateResolver;
    private final TransactionTemplate batchItemTransactionTemplate;
    private Clock clock = Clock.systemUTC();

    public GraphExtractionApplicationServiceImpl(
            AiFacade aiFacade,
            ObjectMapper objectMapper,
            GraphMaterialContentResolver contentResolver,
            GraphMaterialGraphLoader graphLoader,
            GraphSnapshotResolver snapshotSupport,
            GraphSchemaResolver schemaSupport,
            GraphMaterialGraphSaver graphSaver,
            GraphMaterialStatsRefresher statsRefresher,
            GraphDocumentMerger documentMerger,
            GraphMaterialRepository materialRepository,
            GraphExtractionTaskRepository taskRepository,
            GraphExtractionTaskOperator taskOperator,
            GraphTaskCandidateResolver candidateResolver,
            PlatformTransactionManager transactionManager) {
        this.aiFacade = aiFacade;
        this.objectMapper = objectMapper;
        this.contentResolver = contentResolver;
        this.graphLoader = graphLoader;
        this.snapshotSupport = snapshotSupport;
        this.schemaSupport = schemaSupport;
        this.graphSaver = graphSaver;
        this.statsRefresher = statsRefresher;
        this.documentMerger = documentMerger;
        this.materialRepository = materialRepository;
        this.taskRepository = taskRepository;
        this.taskOperator = taskOperator;
        this.candidateResolver = candidateResolver;
        this.batchItemTransactionTemplate = new TransactionTemplate(transactionManager);
        this.batchItemTransactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
    }

    GraphExtractionApplicationServiceImpl useClock(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
        return this;
    }

    @Override
    @Transactional
    public GraphExtractionTaskResult createExtraction(GraphExtractionCommand command) {
        requireIdempotencyKey(command == null ? null : command.idempotencyKey());
        GraphExtractionTask existing = taskRepository.getByIdempotencyKey(command.idempotencyKey());
        if (existing != null) {
            return toTaskResult(existing);
        }
        ContentRef materialRef = requireMaterialRef(command.materialRef());
        GraphMaterialContentSnapshotDto snapshot = contentResolver.resolveWorkbench(materialRef);
        GraphMaterial material = materialForExtraction(materialRef, snapshot.title());
        rejectActiveTask(material);
        GraphExtractionTask task =
                newTask(material, snapshot, command.idempotencyKey(), command.batchId(), command.requestedBy());
        GraphExtractionTaskId taskId = taskRepository.insert(task);
        task.setId(taskId);
        material.setCurrentExtractionTaskId(taskId);
        updateMaterial(material);
        statsRefresher.refresh(material);
        AiBatchJobActionFacadeResponse aiBatch = aiFacade.submitKnowledgeGraphExtraction(
                extractionRequest(materialRef, snapshot, command.requestedBy()));
        if (aiBatch != null && aiBatch.getBatchId() != null) {
            task.setAiBatchId(aiBatch.getBatchId());
            task.start();
            updateTask(task, task.getLockVersion());
        }
        return toTaskResult(task);
    }

    @Override
    public GraphExtractionBatchResult createBatchExtraction(GraphExtractionBatchCommand command) {
        List<ContentRef> materialRefs =
                command == null || command.materialRefs() == null ? List.of() : command.materialRefs();
        if (materialRefs.isEmpty()
                && command != null
                && command.volumeCode() != null
                && !command.volumeCode().isBlank()) {
            throw new BizException(
                    "GRAPH_TASK_VOLUME_SELECTION_UNSUPPORTED",
                    "graph.task.volume-selection-unsupported",
                    "Graph extraction volume selection is not supported by the server yet");
        }
        String batchId = command == null
                        || command.idempotencyKey() == null
                        || command.idempotencyKey().isBlank()
                ? UUID.randomUUID().toString()
                : command.idempotencyKey();
        List<GraphExtractionTaskResult> results = new java.util.ArrayList<>();
        for (int index = 0; index < materialRefs.size(); index++) {
            ContentRef materialRef = materialRefs.get(index);
            String itemIdempotencyKey = batchId + ":" + index;
            try {
                results.add(batchItemTransactionTemplate.execute(status -> createExtraction(new GraphExtractionCommand(
                        materialRef, itemIdempotencyKey, batchId, command == null ? null : command.requestedBy()))));
            } catch (BizException ex) {
                results.add(new GraphExtractionTaskResult(
                        null,
                        materialRef,
                        "FAILED",
                        null,
                        0,
                        0,
                        batchId,
                        null,
                        null,
                        0,
                        ex.getMessage(),
                        Instant.now(clock),
                        Instant.now(clock),
                        null,
                        null,
                        null));
            }
        }
        return new GraphExtractionBatchResult(batchId, results);
    }

    @Override
    public PageResult<GraphExtractionTaskResult> pageTasks(GraphTaskQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        var page = taskRepository.listWithMaterialTitle(
                query == null ? null : query.contentRefs(),
                query == null ? null : query.batchId(),
                GraphExtractionExecutionStatus.from(query == null ? null : query.executionStatus()),
                GraphExtractionDisposition.from(query == null ? null : query.disposition()),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        return PageResult.of(
                page.getPageNo(),
                page.getPageSize(),
                page.getTotalCount(),
                page.getRecords().stream()
                        .map(item -> toTaskResult(syncTaskFromAi(item.task()), item.materialTitle()))
                        .toList());
    }

    @Override
    public GraphExtractionTaskDetailResult getTask(GraphTaskDetailQuery query) {
        GraphExtractionTask task = syncTaskFromAi(requireTask(query == null ? null : query.taskId()));
        return new GraphExtractionTaskDetailResult(
                toTaskResult(task),
                List.of(),
                relatedTasks(task).stream().map(this::toTaskResult).toList(),
                candidateResolver.resolve(task));
    }

    @Override
    @Transactional
    public GraphExtractionTaskResult retryTask(GraphExtractionRetryCommand command) {
        GraphExtractionTask task =
                requireVersionedTask(command == null ? null : command.taskId(), command.taskLockVersion());
        requireExpectedStatus(task, command.expectedExecutionStatus());
        if (!hasUsableContentSnapshot(task.getContentSnapshotJson())) {
            task.setContentSnapshotJson(snapshotJson(contentResolver.resolveWorkbench(task.getContentRef())));
        }
        taskOperator.retry(task);
        updateTask(task, command.taskLockVersion());
        GraphMaterial material = materialRepository.getByContentRef(task.getContentRef());
        if (material == null) {
            throw new BizException("Graph material does not exist");
        }
        material.setCurrentExtractionTaskId(task.getId());
        updateMaterial(material);
        AiBatchJobActionFacadeResponse aiBatch =
                aiFacade.submitKnowledgeGraphExtraction(extractionRequest(task, command.requestedBy()));
        if (aiBatch != null && aiBatch.getBatchId() != null) {
            task.setAiBatchId(aiBatch.getBatchId());
            task.start();
            updateTask(task, task.getLockVersion());
        }
        statsRefresher.refresh(task.getContentRef());
        return toTaskResult(task);
    }

    @Override
    public int syncActiveTasks() {
        return syncTasksWithStatus(GraphExtractionExecutionStatus.PENDING, 100)
                + syncTasksWithStatus(GraphExtractionExecutionStatus.RUNNING, 100);
    }

    @Override
    public int syncActiveTasks(List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return 0;
        }
        return taskRepository.listLatestByMaterialIds(materialIds).stream()
                .filter(this::active)
                .map(this::syncTaskFromAi)
                .mapToInt(task -> 1)
                .sum();
    }

    @Override
    @Transactional
    public GraphExtractionTaskResult cancelTask(GraphExtractionCancelCommand command) {
        GraphExtractionTask task =
                requireVersionedTask(command == null ? null : command.taskId(), command.taskLockVersion());
        requireExpectedStatus(task, command.expectedExecutionStatus());
        taskOperator.cancel(task, Instant.now(clock));
        updateTask(task, command.taskLockVersion());
        clearActiveTask(task);
        statsRefresher.refresh(task.getContentRef());
        return toTaskResult(task);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public GraphMaterialResult applyCandidate(GraphExtractionCandidateApplyCommand command) {
        GraphExtractionTask task =
                requireVersionedTask(command == null ? null : command.taskId(), command.taskLockVersion());
        requireExpectedStatus(task, command.expectedExecutionStatus());
        requireExpectedDisposition(task, command.expectedDisposition());
        AiCandidateFacadeDto candidate =
                aiFacade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                        .candidateId(task.getCandidateId())
                        .contentType(ContentRefCodec.toContentType(task.getContentRef()))
                        .contentId(ContentRefCodec.toValue(task.getContentRef()))
                        .capability(AI_CAPABILITY)
                        .build());
        GraphDocumentDto document = snapshotSupport.parseCandidate(candidate.getResultPayload());
        List<GraphValidationIssueResult> issues = schemaSupport.validateLoose(document);
        if (!issues.isEmpty()) {
            throw new BizException("Graph extraction candidate does not match graph schema");
        }
        GraphMaterialGraph graph = graphLoader.require(task.getContentRef());
        graph.material().setCurrentExtractionTaskId(null);
        GraphDocumentDto documentToApply = documentForMode(graph, document, command.applyMode());
        GraphMaterialGraph saved =
                graphSaver.replaceDocument(graph, documentToApply, GraphSourceType.AI, command.materialLockVersion());
        GraphMaterialResult result = GraphApplicationAssembler.toMaterialResult(saved);
        taskOperator.adopt(task, dispositionFor(command.applyMode()), Instant.now(clock), purgeAfter());
        updateTask(task, command.taskLockVersion());
        statsRefresher.refresh(saved.material());
        aiFacade.markCandidateApplied(MarkAiCandidateAppliedFacadeRequest.builder()
                .candidateId(candidate.getCandidateId())
                .resultFormat(candidate.getResultFormat())
                .resultPayload(candidate.getResultPayload())
                .appliedAt(Instant.now(clock))
                .build());
        return result;
    }

    @Override
    @Transactional
    public GraphExtractionTaskResult discardCandidate(GraphExtractionCandidateDiscardCommand command) {
        GraphExtractionTask task =
                requireVersionedTask(command == null ? null : command.taskId(), command.taskLockVersion());
        requireExpectedStatus(task, command.expectedExecutionStatus());
        requireExpectedDisposition(task, command.expectedDisposition());
        taskOperator.discard(task, Instant.now(clock), purgeAfter());
        updateTask(task, command.taskLockVersion());
        statsRefresher.refresh(task.getContentRef());
        aiFacade.rejectCandidate(RejectAiCandidateFacadeRequest.builder()
                .candidateId(task.getCandidateId())
                .errorType("DISCARDED")
                .errorMessage(command.reason())
                .build());
        return toTaskResult(task);
    }

    @Override
    @Transactional
    public GraphExtractionTaskResult regenerateTask(GraphExtractionRegenerateCommand command) {
        GraphExtractionTask previous =
                requireVersionedTask(command == null ? null : command.taskId(), command.taskLockVersion());
        requireExpectedStatus(previous, command.expectedExecutionStatus());
        if (command.expectedDisposition() != null) {
            requireExpectedDisposition(previous, command.expectedDisposition());
        }
        requireIdempotencyKey(command.idempotencyKey());
        GraphExtractionTask existing = taskRepository.getByIdempotencyKey(command.idempotencyKey());
        if (existing != null) {
            return toTaskResult(existing);
        }
        GraphMaterialContentSnapshotDto snapshot = contentResolver.resolveWorkbench(previous.getContentRef());
        GraphMaterial material = materialForExtraction(previous.getContentRef(), snapshot.title());
        rejectActiveTask(material);
        GraphExtractionTask nextTask =
                newTask(material, snapshot, command.idempotencyKey(), previous.getBatchId(), command.requestedBy());
        taskOperator.regenerate(previous, nextTask);
        GraphExtractionTaskId nextTaskId = taskRepository.insert(nextTask);
        nextTask.setId(nextTaskId);
        taskOperator.supersede(previous, nextTaskId, Instant.now(clock), purgeAfter());
        updateTask(previous, command.taskLockVersion());
        material.setCurrentExtractionTaskId(nextTaskId);
        updateMaterial(material);
        statsRefresher.refresh(material);
        AiBatchJobActionFacadeResponse aiBatch = aiFacade.submitKnowledgeGraphExtraction(
                extractionRequest(previous.getContentRef(), snapshot, command.requestedBy()));
        if (aiBatch != null && aiBatch.getBatchId() != null) {
            nextTask.setAiBatchId(aiBatch.getBatchId());
            nextTask.start();
            updateTask(nextTask, nextTask.getLockVersion());
        }
        return toTaskResult(nextTask);
    }

    private GraphMaterial materialForExtraction(ContentRef materialRef, String title) {
        graphLoader.getOrCreate(materialRef, title);
        GraphMaterial material = materialRepository.getByContentRef(materialRef);
        if (material == null || material.getId() == null) {
            throw new BizException("Graph material initialization failed");
        }
        material.requireEditable();
        return material;
    }

    private GraphExtractionTask newTask(
            GraphMaterial material,
            GraphMaterialContentSnapshotDto snapshot,
            String idempotencyKey,
            String batchId,
            Long requestedBy) {
        return new GraphExtractionTask(
                null,
                material.getId(),
                material.getContentRef(),
                snapshotJson(snapshot),
                "{}",
                "{}",
                "{}",
                GraphExtractionExecutionStatus.PENDING,
                null,
                1,
                0,
                batchId,
                null,
                null,
                "QUEUED",
                0,
                null,
                idempotencyKey,
                null,
                null,
                null,
                Instant.now(clock),
                null,
                null,
                null);
    }

    private KnowledgeGraphExtractionJobFacadeRequest extractionRequest(
            ContentRef materialRef, GraphMaterialContentSnapshotDto snapshot, Long requestedBy) {
        return extractionRequest(materialRef, snapshot.title(), snapshotJson(snapshot), requestedBy);
    }

    private KnowledgeGraphExtractionJobFacadeRequest extractionRequest(GraphExtractionTask task, Long requestedBy) {
        return extractionRequest(
                task.getContentRef(),
                snapshotTitle(task.getContentSnapshotJson()),
                task.getContentSnapshotJson(),
                requestedBy);
    }

    private KnowledgeGraphExtractionJobFacadeRequest extractionRequest(
            ContentRef materialRef, String title, String snapshotJson, Long requestedBy) {
        return KnowledgeGraphExtractionJobFacadeRequest.builder()
                .scope(AI_SCOPE)
                .contentType(ContentRefCodec.toContentType(materialRef))
                .contentId(ContentRefCodec.toValue(materialRef))
                .contentTitle(title)
                .contentSnapshotJson(snapshotJson)
                .requestedBy(requestedBy)
                .build();
    }

    private List<GraphExtractionTask> relatedTasks(GraphExtractionTask task) {
        if (task == null || task.getMaterialId() == null) {
            return List.of();
        }
        return taskRepository.listByMaterialId(task.getMaterialId()).stream()
                .filter(candidate ->
                        candidate.getId() != null && !candidate.getId().equals(task.getId()))
                .toList();
    }

    private GraphExtractionTask requireVersionedTask(Long taskId, long expectedLockVersion) {
        GraphExtractionTask task = requireTask(taskId);
        if (task.getLockVersion() != expectedLockVersion) {
            throw new BizException(
                    "GRAPH_TASK_LOCK_CONFLICT",
                    "graph.task.lock-conflict",
                    "Graph extraction task lock version mismatch");
        }
        return task;
    }

    private GraphExtractionTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new BizException("Graph extraction task id is required");
        }
        GraphExtractionTask task = taskRepository.getById(new GraphExtractionTaskId(taskId));
        if (task == null) {
            throw new BizException("Graph extraction task does not exist");
        }
        return task;
    }

    private void rejectActiveTask(GraphMaterial material) {
        if (material.getCurrentExtractionTaskId() != null
                || taskRepository.listByMaterialId(material.getId()).stream().anyMatch(this::active)) {
            throw new BizException(
                    "GRAPH_TASK_ACTIVE_EXISTS",
                    "graph.task.active-exists",
                    "Graph material already has an active extraction task");
        }
    }

    private boolean active(GraphExtractionTask task) {
        return task != null
                && (task.getExecutionStatus() == GraphExtractionExecutionStatus.PENDING
                        || task.getExecutionStatus() == GraphExtractionExecutionStatus.RUNNING);
    }

    private void clearActiveTask(GraphExtractionTask task) {
        GraphMaterial material = materialRepository.getByContentRef(task.getContentRef());
        if (material != null
                && material.getCurrentExtractionTaskId() != null
                && material.getCurrentExtractionTaskId().equals(task.getId())) {
            material.setCurrentExtractionTaskId(null);
            updateMaterial(material);
        }
    }

    private void updateMaterial(GraphMaterial material) {
        if (materialRepository.updateIfLockVersion(material, material.getLockVersion()) != 1) {
            throw new BizException("Graph material lock version mismatch");
        }
    }

    private void updateTask(GraphExtractionTask task, long expectedLockVersion) {
        if (taskRepository.updateIfLockVersion(task, expectedLockVersion) != 1) {
            throw new BizException(
                    "GRAPH_TASK_LOCK_CONFLICT",
                    "graph.task.lock-conflict",
                    "Graph extraction task lock version mismatch");
        }
        task.setLockVersion(expectedLockVersion + 1);
    }

    private void requireExpectedStatus(GraphExtractionTask task, String expectedStatus) {
        GraphExtractionExecutionStatus expected = GraphExtractionExecutionStatus.from(expectedStatus);
        if (expected != null && task.getExecutionStatus() != expected) {
            throw new BizException(
                    "GRAPH_TASK_STATE_CONFLICT",
                    "graph.task.state-conflict",
                    "Graph extraction task execution status mismatch");
        }
    }

    private void requireExpectedDisposition(GraphExtractionTask task, String expectedDisposition) {
        GraphExtractionDisposition expected = GraphExtractionDisposition.from(expectedDisposition);
        if (expected != null && task.getDisposition() != expected) {
            throw new BizException(
                    "GRAPH_TASK_STATE_CONFLICT",
                    "graph.task.state-conflict",
                    "Graph extraction task disposition mismatch");
        }
    }

    private GraphExtractionDisposition dispositionFor(GraphMaterialApplyMode applyMode) {
        return applyMode == GraphMaterialApplyMode.REPLACE
                ? GraphExtractionDisposition.ADOPTED_REPLACE
                : GraphExtractionDisposition.ADOPTED_MERGE;
    }

    private Instant purgeAfter() {
        return Instant.now(clock).plusSeconds(PURGE_AFTER_DAYS * 24L * 60L * 60L);
    }

    private GraphDocumentDto documentForMode(
            GraphMaterialGraph graph, GraphDocumentDto document, GraphMaterialApplyMode applyMode) {
        if (applyMode == null || applyMode == GraphMaterialApplyMode.REPLACE) {
            return document;
        }
        return documentMerger.merge(snapshotSupport.parseImport(snapshotSupport.serialize(graph)), document);
    }

    private GraphExtractionTaskResult toTaskResult(GraphExtractionTask task) {
        return toTaskResult(task, null);
    }

    private GraphExtractionTaskResult toTaskResult(GraphExtractionTask task, String materialTitle) {
        if (task == null) {
            return null;
        }
        return new GraphExtractionTaskResult(
                task.getId() == null ? null : task.getId().value(),
                task.getContentRef(),
                task.getExecutionStatus() == null
                        ? null
                        : task.getExecutionStatus().value(),
                task.getDisposition() == null ? null : task.getDisposition().value(),
                task.getAttemptNo(),
                task.getLockVersion(),
                task.getBatchId(),
                task.getCandidateId(),
                task.getCurrentStage(),
                task.getProgress(),
                task.getFailureReason(),
                task.getRequestedAt(),
                task.getCompletedAt(),
                task.getDisposedAt(),
                task.getPurgeAfter(),
                materialTitle);
    }

    private GraphExtractionTask syncTaskFromAi(GraphExtractionTask task) {
        if (task == null
                || task.getAiBatchId() == null
                || task.getExecutionStatus() == GraphExtractionExecutionStatus.SUCCEEDED
                || task.getExecutionStatus() == GraphExtractionExecutionStatus.FAILED
                || task.getExecutionStatus() == GraphExtractionExecutionStatus.CANCELLED) {
            return task;
        }
        AiBatchJobFacadeResponse batch = aiFacade.getBatchJob(task.getAiBatchId());
        if (batch == null || batch.getStatus() == null) {
            return task;
        }
        GraphExtractionExecutionStatus originalStatus = task.getExecutionStatus();
        switch (batch.getStatus()) {
            case "RUNNING" -> startPendingTask(task);
            case "SUCCEEDED", "PARTIAL" -> succeedTaskFromAi(task);
            case "FAILED" -> failTaskFromAi(task, batch);
            case "CANCELLED" -> cancelTaskFromAi(task, batch.getCompletedAt());
            default -> {
                return task;
            }
        }
        if (task.getExecutionStatus() != originalStatus) {
            try {
                updateTask(task, task.getLockVersion());
            } catch (BizException ex) {
                if (!"GRAPH_TASK_LOCK_CONFLICT".equals(ex.getCode())) {
                    throw ex;
                }
                return requireTask(task.getId().value());
            }
            if (task.getExecutionStatus() != GraphExtractionExecutionStatus.PENDING
                    && task.getExecutionStatus() != GraphExtractionExecutionStatus.RUNNING) {
                clearActiveTask(task);
            }
            statsRefresher.refresh(task.getContentRef());
        }
        return task;
    }

    private int syncTasksWithStatus(GraphExtractionExecutionStatus status, int limit) {
        return taskRepository.page(null, null, status, null, 1, limit).getRecords().stream()
                .map(this::syncTaskFromAi)
                .mapToInt(task -> 1)
                .sum();
    }

    private void startPendingTask(GraphExtractionTask task) {
        if (task.getExecutionStatus() == GraphExtractionExecutionStatus.PENDING) {
            task.start();
        }
    }

    private void succeedTaskFromAi(GraphExtractionTask task) {
        AiCandidateFacadeDto candidate = aiFacade.getLatestCandidateByBatch(task.getAiBatchId());
        if (candidate == null || candidate.getCandidateId() == null) {
            startPendingTask(task);
            task.fail("CANDIDATE_UNAVAILABLE", "AI batch completed without a candidate", Instant.now(clock));
            return;
        }
        startPendingTask(task);
        task.succeed(candidate.getCandidateId(), "CANDIDATE_READY", 100, Instant.now(clock));
    }

    private void failTaskFromAi(GraphExtractionTask task, AiBatchJobFacadeResponse batch) {
        startPendingTask(task);
        task.fail(
                "FAILED",
                batch.getFailureSummaryJson() == null
                                || batch.getFailureSummaryJson().isBlank()
                        ? "AI batch execution failed"
                        : batch.getFailureSummaryJson(),
                batch.getCompletedAt() == null ? Instant.now(clock) : batch.getCompletedAt());
    }

    private void cancelTaskFromAi(GraphExtractionTask task, Instant completedAt) {
        task.cancel(completedAt == null ? Instant.now(clock) : completedAt);
    }

    private String snapshotJson(GraphMaterialContentSnapshotDto snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new BizException("Graph extraction content snapshot cannot be serialized");
        }
    }

    private String snapshotTitle(String snapshotJson) {
        try {
            return objectMapper.readTree(snapshotJson).path("title").asText();
        } catch (JsonProcessingException ex) {
            throw new BizException("Graph extraction content snapshot cannot be read");
        }
    }

    private boolean hasUsableContentSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return false;
        }
        try {
            return !objectMapper.readTree(snapshotJson).path("title").asText().isBlank();
        } catch (JsonProcessingException ex) {
            return false;
        }
    }

    private ContentRef requireMaterialRef(ContentRef materialRef) {
        if (materialRef == null) {
            throw new BizException("Graph material ref is required");
        }
        return materialRef;
    }

    private void requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BizException("Graph extraction idempotency key is required");
        }
    }
}
