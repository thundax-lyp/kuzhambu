package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionBatchCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionDeleteCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRetryCommand;
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
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTaskDeleteReceipt;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.operator.GraphExtractionTaskOperator;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskDeleteReceiptRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

class GraphExtractionApplicationServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-08-17T00:00:00Z");
    private static final PlatformTransactionManager TRANSACTION_MANAGER = new PlatformTransactionManager() {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {}

        @Override
        public void rollback(TransactionStatus status) {}
    };

    private final AiFacade aiFacade = mock(AiFacade.class);
    private final GraphMaterialContentResolver contentResolver = mock(GraphMaterialContentResolver.class);
    private final GraphMaterialGraphLoader graphLoader = mock(GraphMaterialGraphLoader.class);
    private final GraphMaterialRepository materialRepository = mock(GraphMaterialRepository.class);
    private final GraphExtractionTaskRepository taskRepository = mock(GraphExtractionTaskRepository.class);
    private final GraphExtractionTaskDeleteReceiptRepository taskDeleteReceiptRepository =
            mock(GraphExtractionTaskDeleteReceiptRepository.class);
    private final GraphMaterialStatsRefresher statsRefresher = mock(GraphMaterialStatsRefresher.class);
    private final GraphTaskCandidateResolver candidateResolver = new GraphTaskCandidateResolver(aiFacade);

    private final GraphExtractionApplicationServiceImpl service = new GraphExtractionApplicationServiceImpl(
                    aiFacade,
                    new ObjectMapper(),
                    contentResolver,
                    graphLoader,
                    mock(GraphSnapshotResolver.class),
                    mock(GraphSchemaResolver.class),
                    mock(GraphMaterialGraphSaver.class),
                    statsRefresher,
                    mock(GraphDocumentMerger.class),
                    materialRepository,
                    taskRepository,
                    taskDeleteReceiptRepository,
                    new GraphExtractionTaskOperator(),
                    candidateResolver,
                    TRANSACTION_MANAGER)
            .useClock(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void shouldCreateTaskAndRejectDuplicateActiveTask() {
        ContentRef ref = ref(1001L);
        when(contentResolver.resolveWorkbench(ref)).thenReturn(snapshot(ref));
        when(materialRepository.getByContentRef(ref)).thenReturn(material(11L, ref, null));
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of());
        when(taskRepository.insert(any())).thenReturn(new GraphExtractionTaskId(7001L));
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(materialRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(aiFacade.submitKnowledgeGraphExtraction(any()))
                .thenReturn(
                        AiBatchJobActionFacadeResponse.builder().batchId(9001L).build());

        var result = service.createExtraction(new GraphExtractionCommand(ref, "idem-1", 1L));

        assertThat(result.taskId()).isEqualTo(7001L);
        assertThat(result.executionStatus()).isEqualTo("RUNNING");
        verify(aiFacade).submitKnowledgeGraphExtraction(any());
        verify(statsRefresher).refresh(any(GraphMaterial.class));

        when(materialRepository.getByContentRef(ref)).thenReturn(material(11L, ref, new GraphExtractionTaskId(7001L)));
        assertThatThrownBy(() -> service.createExtraction(new GraphExtractionCommand(ref, "idem-2", 1L)))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo("GRAPH_TASK_ACTIVE_EXISTS");
    }

    @Test
    void shouldKeepBatchInputOrderWhenOneMaterialFails() {
        ContentRef first = ref(1001L);
        ContentRef second = ref(1002L);
        when(contentResolver.resolveWorkbench(first)).thenReturn(snapshot(first));
        when(contentResolver.resolveWorkbench(second)).thenReturn(snapshot(second));
        when(materialRepository.getByContentRef(first)).thenReturn(material(11L, first, null));
        when(materialRepository.getByContentRef(second))
                .thenReturn(material(12L, second, new GraphExtractionTaskId(8001L)));
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of());
        when(taskRepository.insert(any())).thenReturn(new GraphExtractionTaskId(7001L));
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(materialRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(aiFacade.submitKnowledgeGraphExtraction(any()))
                .thenReturn(
                        AiBatchJobActionFacadeResponse.builder().batchId(9001L).build());

        var result =
                service.createBatchExtraction(new GraphExtractionBatchCommand(List.of(first, second), "batch-a", 1L));

        assertThat(result.tasks()).hasSize(2);
        assertThat(result.tasks().get(0).contentRef()).isEqualTo(first);
        assertThat(result.tasks().get(0).executionStatus()).isEqualTo("RUNNING");
        assertThat(result.tasks().get(1).contentRef()).isEqualTo(second);
        assertThat(result.tasks().get(1).executionStatus()).isEqualTo("FAILED");
    }

    @Test
    void shouldReportCandidateUnavailableThroughAiFacadeOnly() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setExecutionStatus(GraphExtractionExecutionStatus.SUCCEEDED);
        task.setDisposition(GraphExtractionDisposition.PENDING);
        task.setCandidateId(9001L);
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(task);
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of(task));
        when(aiFacade.getCandidate(any())).thenReturn(null);

        assertThatThrownBy(() -> service.getTask(new GraphTaskDetailQuery(7001L)))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo("GRAPH_CANDIDATE_UNAVAILABLE");

        verify(taskRepository, never()).listByBatchId(any());
    }

    @Test
    void shouldSyncPendingTaskFromCompletedAiBatch() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setAiBatchId(9001L);
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(9101L)
                .batchId(9001L)
                .capability("KNOWLEDGE_GRAPH_EXTRACT")
                .contentType("SANCAI_ENTRY")
                .contentId(1001L)
                .resultFormat("json")
                .resultPayload("{}")
                .build();
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(task);
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of(task));
        when(aiFacade.getBatchJob(9001L))
                .thenReturn(AiBatchJobFacadeResponse.builder()
                        .batchId(9001L)
                        .status("SUCCEEDED")
                        .completedAt(NOW)
                        .build());
        when(aiFacade.getLatestCandidateByBatch(9001L)).thenReturn(candidate);
        when(aiFacade.getCandidate(any())).thenReturn(candidate);

        var result = service.getTask(new GraphTaskDetailQuery(7001L));

        assertThat(result.task().executionStatus()).isEqualTo("SUCCEEDED");
        assertThat(result.task().disposition()).isEqualTo("PENDING");
        assertThat(result.task().candidateId()).isEqualTo(9101L);
        verify(taskRepository).updateIfLockVersion(any(GraphExtractionTask.class), any(Long.class));
    }

    @Test
    void shouldResetRunningTasksToPendingAtStartupWithoutCallingAi() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setExecutionStatus(GraphExtractionExecutionStatus.RUNNING);
        task.setCurrentStage("EXTRACT");
        task.setProgress(40);
        when(taskRepository.page(null, null, GraphExtractionExecutionStatus.RUNNING, null, 1, 100))
                .thenReturn(PageResult.of(1, 100, 1, List.of(task)));
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);

        int recoveredCount = service.recoverActiveTasksAtStartup();

        assertThat(recoveredCount).isEqualTo(1);
        assertThat(task.getExecutionStatus()).isEqualTo(GraphExtractionExecutionStatus.PENDING);
        assertThat(task.getCurrentStage()).isEqualTo("PENDING");
        assertThat(task.getProgress()).isEqualTo(40);
        verify(aiFacade, never()).getBatchJob(any());
    }

    @Test
    void shouldStartPendingRecoveredTaskWhenAiBatchIsRunning() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setExecutionStatus(GraphExtractionExecutionStatus.PENDING);
        task.setAiBatchId(9001L);
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(task);
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of(task));
        when(aiFacade.getBatchJob(9001L))
                .thenReturn(AiBatchJobFacadeResponse.builder()
                        .batchId(9001L)
                        .status("RUNNING")
                        .build());

        var result = service.getTask(new GraphTaskDetailQuery(7001L));

        assertThat(result.task().executionStatus()).isEqualTo("RUNNING");
        verify(taskRepository).updateIfLockVersion(any(GraphExtractionTask.class), any(Long.class));
    }

    @Test
    void shouldFailTaskWhenCompletedAiBatchHasNoCandidate() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setAiBatchId(9001L);
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(task);
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of(task));
        when(aiFacade.getBatchJob(9001L))
                .thenReturn(AiBatchJobFacadeResponse.builder()
                        .batchId(9001L)
                        .status("SUCCEEDED")
                        .completedAt(NOW)
                        .build());

        var result = service.getTask(new GraphTaskDetailQuery(7001L));

        assertThat(result.task().executionStatus()).isEqualTo("FAILED");
        assertThat(result.task().currentStage()).isEqualTo("CANDIDATE_UNAVAILABLE");
        assertThat(result.task().failureReason()).isEqualTo("AI batch completed without a candidate");
    }

    @Test
    void shouldExposeAiBatchFailureSummaryOnTask() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setAiBatchId(9001L);
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(task);
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(aiFacade.getBatchJob(9001L))
                .thenReturn(AiBatchJobFacadeResponse.builder()
                        .batchId(9001L)
                        .status("FAILED")
                        .failureSummaryJson("worker timeout")
                        .completedAt(NOW)
                        .build());

        var result = service.getTask(new GraphTaskDetailQuery(7001L));

        assertThat(result.task().executionStatus()).isEqualTo("FAILED");
        assertThat(result.task().failureReason()).isEqualTo("worker timeout");
    }

    @Test
    void shouldReturnLatestTaskWhenAiSyncLosesLockRace() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask staleTask = task(7001L, 11L, ref);
        staleTask.setAiBatchId(9001L);
        GraphExtractionTask synchronizedTask = task(7001L, 11L, ref);
        synchronizedTask.setAiBatchId(9001L);
        synchronizedTask.setExecutionStatus(GraphExtractionExecutionStatus.SUCCEEDED);
        synchronizedTask.setCandidateId(9101L);
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(9101L)
                .batchId(9001L)
                .capability("KNOWLEDGE_GRAPH_EXTRACT")
                .contentType("SANCAI_ENTRY")
                .contentId(1001L)
                .resultFormat("json")
                .resultPayload("{}")
                .build();
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(staleTask, synchronizedTask);
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(0);
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of(synchronizedTask));
        when(aiFacade.getBatchJob(9001L))
                .thenReturn(AiBatchJobFacadeResponse.builder()
                        .batchId(9001L)
                        .status("SUCCEEDED")
                        .completedAt(NOW)
                        .build());
        when(aiFacade.getLatestCandidateByBatch(9001L)).thenReturn(candidate);
        when(aiFacade.getCandidate(any())).thenReturn(candidate);

        var result = service.getTask(new GraphTaskDetailQuery(7001L));

        assertThat(result.task().executionStatus()).isEqualTo("SUCCEEDED");
        assertThat(result.task().candidateId()).isEqualTo(9101L);
    }

    @Test
    void shouldSubmitNewAiBatchWhenRetryingFailedTask() throws Exception {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setExecutionStatus(GraphExtractionExecutionStatus.FAILED);
        task.setAiBatchId(9001L);
        task.setContentSnapshotJson(new ObjectMapper().writeValueAsString(snapshot(ref)));
        GraphMaterial material = material(11L, ref, null);
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(task);
        when(taskRepository.getByIdempotencyKey("retry-1")).thenReturn(null);
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of(task));
        when(taskRepository.insert(any())).thenReturn(new GraphExtractionTaskId(7002L));
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(materialRepository.getByContentRef(ref)).thenReturn(material);
        when(materialRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(contentResolver.resolveWorkbench(ref)).thenReturn(snapshot(ref));
        when(aiFacade.submitKnowledgeGraphExtraction(any()))
                .thenReturn(
                        AiBatchJobActionFacadeResponse.builder().batchId(9002L).build());

        var result = service.retryTask(new GraphExtractionRetryCommand(7001L, 3L, "FAILED", "retry-1", 1L));

        assertThat(result.executionStatus()).isEqualTo("RUNNING");
        assertThat(result.taskId()).isEqualTo(7002L);
        assertThat(result.attemptNo()).isEqualTo(1);
        assertThat(result.categoryName()).isEqualTo("分类");
        assertThat(result.volumeName()).isEqualTo("卷一");
        assertThat(task.getAiBatchId()).isEqualTo(9001L);
        assertThat(task.getSupersededByTaskId()).isEqualTo(new GraphExtractionTaskId(7002L));
        assertThat(material.getCurrentExtractionTaskId()).isEqualTo(new GraphExtractionTaskId(7002L));
        verify(contentResolver).resolveWorkbench(ref);
        verify(aiFacade).submitKnowledgeGraphExtraction(any());
    }

    @Test
    void shouldReturnExistingRetryBeforeCheckingSourceTaskVersion() {
        GraphExtractionTask existing = task(7002L, 11L, ref(1001L));
        existing.setExecutionStatus(GraphExtractionExecutionStatus.RUNNING);
        when(taskRepository.getByIdempotencyKey("retry-1")).thenReturn(existing);

        var result = service.retryTask(new GraphExtractionRetryCommand(7001L, 2L, "FAILED", "retry-1", 1L));

        assertThat(result.taskId()).isEqualTo(7002L);
        assertThat(result.executionStatus()).isEqualTo("RUNNING");
        verify(taskRepository, never()).getById(any());
        verifyNoInteractions(contentResolver, aiFacade);
    }

    @Test
    void shouldAlwaysRefreshContentSnapshotWhenRetryingFailedTask() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setExecutionStatus(GraphExtractionExecutionStatus.FAILED);
        GraphMaterial material = material(11L, ref, null);
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(task);
        when(taskRepository.getByIdempotencyKey("retry-1")).thenReturn(null);
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of(task));
        when(taskRepository.insert(any())).thenReturn(new GraphExtractionTaskId(7002L));
        when(taskRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(materialRepository.getByContentRef(ref)).thenReturn(material);
        when(materialRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(contentResolver.resolveWorkbench(ref)).thenReturn(snapshot(ref));
        when(aiFacade.submitKnowledgeGraphExtraction(any()))
                .thenReturn(
                        AiBatchJobActionFacadeResponse.builder().batchId(9002L).build());

        var result = service.retryTask(new GraphExtractionRetryCommand(7001L, 3L, "FAILED", "retry-1", 1L));

        assertThat(result.materialTitle()).isEqualTo("素材1001");
        verify(contentResolver).resolveWorkbench(ref);
    }

    @Test
    void shouldDeleteTerminalTask() {
        ContentRef ref = ref(1001L);
        GraphExtractionTask task = task(7001L, 11L, ref);
        task.setExecutionStatus(GraphExtractionExecutionStatus.FAILED);
        when(taskRepository.getById(new GraphExtractionTaskId(7001L))).thenReturn(task);
        when(taskDeleteReceiptRepository.insert(any())).thenReturn(true);
        when(taskRepository.deleteByIdAndLockVersion(new GraphExtractionTaskId(7001L), 3L))
                .thenReturn(1);

        var result = service.deleteTask(new GraphExtractionDeleteCommand(7001L, 3L, "FAILED", "delete-1"));

        assertThat(result.deletedTaskId()).isEqualTo(7001L);
        verify(taskRepository).deleteByIdAndLockVersion(new GraphExtractionTaskId(7001L), 3L);
        verify(statsRefresher).refresh(ref);
    }

    @Test
    void shouldReturnPersistedReceiptForRepeatedDeleteRequest() {
        when(taskDeleteReceiptRepository.getByIdempotencyKey("delete-1"))
                .thenReturn(new GraphExtractionTaskDeleteReceipt("delete-1", new GraphExtractionTaskId(7001L), NOW));

        var result = service.deleteTask(new GraphExtractionDeleteCommand(7001L, 3L, "FAILED", "delete-1"));

        assertThat(result.deletedTaskId()).isEqualTo(7001L);
        verifyNoInteractions(taskRepository);
    }

    private static ContentRef ref(Long id) {
        return new ContentRef("SANCAI_ENTRY", id);
    }

    private static GraphMaterialContentSnapshotDto snapshot(ContentRef ref) {
        return new GraphMaterialContentSnapshotDto(
                ref, "分类", "卷一", "素材" + ref.getContentId(), "摘要", List.of("正文"), List.of(), "DRAFT", "PRIVATE", 1);
    }

    private static GraphMaterial material(Long id, ContentRef ref, GraphExtractionTaskId currentTaskId) {
        return new GraphMaterial(
                id, ref, "素材" + ref.getContentId(), GraphMaterialStatus.DRAFT, null, null, null, currentTaskId, 3L);
    }

    private static GraphExtractionTask task(Long id, Long materialId, ContentRef ref) {
        return new GraphExtractionTask(
                new GraphExtractionTaskId(id),
                materialId,
                ref,
                "{}",
                "{}",
                "{}",
                "{}",
                GraphExtractionExecutionStatus.PENDING,
                null,
                1,
                3,
                null,
                null,
                null,
                "PENDING",
                0,
                null,
                "idem-1",
                null,
                null,
                null,
                NOW,
                null,
                null,
                null);
    }
}
