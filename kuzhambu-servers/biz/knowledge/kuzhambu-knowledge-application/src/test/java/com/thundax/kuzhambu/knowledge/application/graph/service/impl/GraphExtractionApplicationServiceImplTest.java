package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobActionFacadeResponse;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionBatchCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphMaterialContentSnapshotDto;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphDocumentMerger;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialContentResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphLoader;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphMaterialGraphSaver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSnapshotResolver;
import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphTaskCandidateResolver;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskDetailQuery;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.service.GraphExtractionTaskDomainService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphExtractionApplicationServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-08-17T00:00:00Z");

    private final AiFacade aiFacade = mock(AiFacade.class);
    private final GraphMaterialContentResolver contentResolver = mock(GraphMaterialContentResolver.class);
    private final GraphMaterialGraphLoader graphLoader = mock(GraphMaterialGraphLoader.class);
    private final GraphMaterialRepository materialRepository = mock(GraphMaterialRepository.class);
    private final GraphExtractionTaskRepository taskRepository = mock(GraphExtractionTaskRepository.class);
    private final GraphTaskCandidateResolver candidateResolver = new GraphTaskCandidateResolver(aiFacade);

    private final GraphExtractionApplicationServiceImpl service = new GraphExtractionApplicationServiceImpl(
            aiFacade,
            new ObjectMapper(),
            contentResolver,
            graphLoader,
            mock(GraphSnapshotResolver.class),
            mock(GraphSchemaResolver.class),
            mock(GraphMaterialGraphSaver.class),
            mock(GraphDocumentMerger.class),
            materialRepository,
            taskRepository,
            new GraphExtractionTaskDomainService(),
            candidateResolver,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void shouldCreateTaskAndRejectDuplicateActiveTask() {
        ContentRef ref = ref(1001L);
        when(contentResolver.resolveWorkbench(ref)).thenReturn(snapshot(ref));
        when(materialRepository.getByContentRef(ref)).thenReturn(material(11L, ref, null));
        when(taskRepository.listByMaterialId(11L)).thenReturn(List.of());
        when(taskRepository.insert(any())).thenReturn(new GraphExtractionTaskId(7001L));
        when(materialRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(aiFacade.submitKnowledgeGraphExtraction(any()))
                .thenReturn(
                        AiBatchJobActionFacadeResponse.builder().batchId(9001L).build());

        var result = service.createExtraction(new GraphExtractionCommand(ref, "idem-1", 1L));

        assertThat(result.taskId()).isEqualTo(7001L);
        assertThat(result.executionStatus()).isEqualTo("PENDING");
        verify(aiFacade).submitKnowledgeGraphExtraction(any());

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
        when(materialRepository.updateIfLockVersion(any(), any(Long.class))).thenReturn(1);
        when(aiFacade.submitKnowledgeGraphExtraction(any()))
                .thenReturn(
                        AiBatchJobActionFacadeResponse.builder().batchId(9001L).build());

        var result =
                service.createBatchExtraction(new GraphExtractionBatchCommand(List.of(first, second), "batch-a", 1L));

        assertThat(result.tasks()).hasSize(2);
        assertThat(result.tasks().get(0).contentRef()).isEqualTo(first);
        assertThat(result.tasks().get(0).executionStatus()).isEqualTo("PENDING");
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

    private static ContentRef ref(Long id) {
        return new ContentRef("SANCAI_ENTRY", id);
    }

    private static GraphMaterialContentSnapshotDto snapshot(ContentRef ref) {
        return new GraphMaterialContentSnapshotDto(
                ref, "素材" + ref.getContentId(), "摘要", List.of("正文"), List.of(), "DRAFT", "PRIVATE", 1);
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
                "QUEUED",
                0,
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
