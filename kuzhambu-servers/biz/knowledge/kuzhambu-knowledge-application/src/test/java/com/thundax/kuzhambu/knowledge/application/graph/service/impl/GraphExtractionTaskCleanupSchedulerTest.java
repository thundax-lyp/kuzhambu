package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.CleanupKnowledgeGraphCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.CleanupKnowledgeGraphCandidateFacadeResponse;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphExtractionTaskCleanupSchedulerTest {
    private static final Instant NOW = Instant.parse("2026-08-24T00:00:00Z");

    @Test
    void cleanupShouldDeleteTaskAfterAiCandidateCleanup() {
        GraphExtractionTaskRepository repository = mock(GraphExtractionTaskRepository.class);
        AiFacade aiFacade = mock(AiFacade.class);
        GraphExtractionTask task = task(7001L, 9001L);
        when(repository.listPurgeableBefore(NOW, 100)).thenReturn(List.of(task));
        when(repository.deleteByIdAndLockVersion(new GraphExtractionTaskId(7001L), 3L))
                .thenReturn(1);
        when(aiFacade.cleanupKnowledgeGraphCandidate(any()))
                .thenReturn(CleanupKnowledgeGraphCandidateFacadeResponse.builder()
                        .candidateId(9001L)
                        .cleaned(true)
                        .build());

        int count = scheduler(repository, aiFacade).cleanupExpiredTasks();

        assertThat(count).isEqualTo(1);
        ArgumentCaptor<CleanupKnowledgeGraphCandidateFacadeRequest> captor =
                ArgumentCaptor.forClass(CleanupKnowledgeGraphCandidateFacadeRequest.class);
        verify(aiFacade).cleanupKnowledgeGraphCandidate(captor.capture());
        assertThat(captor.getValue().getCandidateId()).isEqualTo(9001L);
        verify(repository).deleteByIdAndLockVersion(new GraphExtractionTaskId(7001L), 3L);
    }

    @Test
    void cleanupShouldKeepTaskWhenAiCleanupFailsForRetry() {
        GraphExtractionTaskRepository repository = mock(GraphExtractionTaskRepository.class);
        AiFacade aiFacade = mock(AiFacade.class);
        GraphExtractionTask task = task(7001L, 9001L);
        when(repository.listPurgeableBefore(NOW, 100)).thenReturn(List.of(task));
        when(aiFacade.cleanupKnowledgeGraphCandidate(any())).thenThrow(new BizException("AI cleanup failed"));

        int count = scheduler(repository, aiFacade).cleanupExpiredTasks();

        assertThat(count).isZero();
        verify(repository, never()).deleteByIdAndLockVersion(any(), any(Long.class));
    }

    @Test
    void cleanupShouldDeleteTaskWithoutCandidate() {
        GraphExtractionTaskRepository repository = mock(GraphExtractionTaskRepository.class);
        AiFacade aiFacade = mock(AiFacade.class);
        GraphExtractionTask task = task(7001L, null);
        when(repository.listPurgeableBefore(NOW, 100)).thenReturn(List.of(task));
        when(repository.deleteByIdAndLockVersion(new GraphExtractionTaskId(7001L), 3L))
                .thenReturn(1);

        int count = scheduler(repository, aiFacade).cleanupExpiredTasks();

        assertThat(count).isEqualTo(1);
        verify(aiFacade, never()).cleanupKnowledgeGraphCandidate(any());
        verify(repository).deleteByIdAndLockVersion(new GraphExtractionTaskId(7001L), 3L);
    }

    @Test
    void startupShouldSynchronizeActiveTasks() {
        GraphExtractionTaskRepository repository = mock(GraphExtractionTaskRepository.class);
        AiFacade aiFacade = mock(AiFacade.class);
        GraphExtractionApplicationService extractionService = mock(GraphExtractionApplicationService.class);

        new GraphExtractionTaskCleanupScheduler(repository, aiFacade, extractionService).onApplicationEvent(null);

        verify(extractionService).recoverActiveTasksAtStartup();
    }

    @Test
    void startupShouldKeepApplicationAvailableWhenSynchronizationFails() {
        GraphExtractionTaskRepository repository = mock(GraphExtractionTaskRepository.class);
        AiFacade aiFacade = mock(AiFacade.class);
        GraphExtractionApplicationService extractionService = mock(GraphExtractionApplicationService.class);
        when(extractionService.recoverActiveTasksAtStartup()).thenThrow(new BizException("AI service unavailable"));

        new GraphExtractionTaskCleanupScheduler(repository, aiFacade, extractionService).onApplicationEvent(null);

        verify(extractionService).recoverActiveTasksAtStartup();
    }

    private static GraphExtractionTaskCleanupScheduler scheduler(
            GraphExtractionTaskRepository repository, AiFacade aiFacade) {
        return new GraphExtractionTaskCleanupScheduler(
                        repository, aiFacade, mock(GraphExtractionApplicationService.class))
                .useClock(Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static GraphExtractionTask task(Long id, Long candidateId) {
        return new GraphExtractionTask(
                new GraphExtractionTaskId(id),
                11L,
                new ContentRef("SANCAI_ENTRY", 1001L),
                "{}",
                "{}",
                "{}",
                "{}",
                GraphExtractionExecutionStatus.SUCCEEDED,
                GraphExtractionDisposition.DISCARDED,
                1,
                3,
                null,
                null,
                candidateId,
                "DONE",
                100,
                null,
                "idem-1",
                null,
                null,
                null,
                Instant.parse("2026-08-17T00:00:00Z"),
                Instant.parse("2026-08-17T00:01:00Z"),
                Instant.parse("2026-08-17T00:02:00Z"),
                NOW);
    }
}
