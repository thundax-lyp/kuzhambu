package com.thundax.kuzhambu.knowledge.domain.graph.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GraphExtractionTaskOperatorTest {
    private final GraphExtractionTaskOperator service = new GraphExtractionTaskOperator();

    @Test
    void retryShouldResetTheSameFailedTask() {
        GraphExtractionTask task = task(GraphExtractionExecutionStatus.FAILED, null);
        task.setAttemptNo(2);
        task.setCurrentStage("EXTRACT");
        task.setProgress(40);
        task.setCompletedAt(Instant.parse("2026-08-17T00:00:00Z"));

        service.retry(task);

        assertEquals(GraphExtractionExecutionStatus.PENDING, task.getExecutionStatus());
        assertEquals(3, task.getAttemptNo());
        assertEquals(0, task.getProgress());
        assertEquals(null, task.getCurrentStage());
        assertEquals(null, task.getCompletedAt());
    }

    @Test
    void retryShouldRejectNonFailedTask() {
        assertThrows(DomainException.class, () -> service.retry(task(GraphExtractionExecutionStatus.RUNNING, null)));
    }

    @Test
    void cancelShouldAcceptOnlyActiveTask() {
        GraphExtractionTask task = task(GraphExtractionExecutionStatus.PENDING, null);
        service.cancel(task, Instant.parse("2026-08-17T00:00:00Z"));
        assertEquals(GraphExtractionExecutionStatus.CANCELLED, task.getExecutionStatus());
        assertThrows(DomainException.class, () -> service.cancel(task, Instant.now()));
    }

    @Test
    void resetToPendingForRecoveryShouldPreserveAttemptAndRequireRunningTask() {
        GraphExtractionTask task = task(GraphExtractionExecutionStatus.RUNNING, null);
        task.setAttemptNo(2);
        task.setProgress(40);

        task.resetToPendingForRecovery();

        assertEquals(GraphExtractionExecutionStatus.PENDING, task.getExecutionStatus());
        assertEquals("PENDING", task.getCurrentStage());
        assertEquals(2, task.getAttemptNo());
        assertEquals(40, task.getProgress());
        assertThrows(DomainException.class, () -> task(GraphExtractionExecutionStatus.PENDING, null)
                .resetToPendingForRecovery());
    }

    @Test
    void adoptDiscardAndSupersedeShouldBeTerminalAndExclusive() {
        Instant disposedAt = Instant.parse("2026-08-17T00:00:00Z");
        Instant purgeAfter = Instant.parse("2026-08-24T00:00:00Z");
        GraphExtractionTask adopted =
                task(GraphExtractionExecutionStatus.SUCCEEDED, GraphExtractionDisposition.PENDING);
        service.adopt(adopted, GraphExtractionDisposition.ADOPTED_MERGE, disposedAt, purgeAfter);
        assertEquals(GraphExtractionDisposition.ADOPTED_MERGE, adopted.getDisposition());
        assertThrows(
                DomainException.class,
                () -> service.discard(adopted, Instant.now(), Instant.now().plusSeconds(604800)));

        GraphExtractionTask superseded =
                task(GraphExtractionExecutionStatus.SUCCEEDED, GraphExtractionDisposition.PENDING);
        service.supersede(superseded, new GraphExtractionTaskId(2L), disposedAt, purgeAfter);
        assertEquals(GraphExtractionDisposition.SUPERSEDED, superseded.getDisposition());
        assertEquals(new GraphExtractionTaskId(2L), superseded.getSupersededByTaskId());
    }

    @Test
    void regenerateShouldLinkNewTaskToItsSource() {
        GraphExtractionTask previous =
                task(GraphExtractionExecutionStatus.SUCCEEDED, GraphExtractionDisposition.PENDING);
        GraphExtractionTask next = task(GraphExtractionExecutionStatus.PENDING, null);
        service.regenerate(previous, next);
        assertEquals(previous.getId(), next.getRegeneratedFromTaskId());
    }

    private GraphExtractionTask task(
            GraphExtractionExecutionStatus executionStatus, GraphExtractionDisposition disposition) {
        GraphExtractionTask task = new GraphExtractionTask();
        task.setId(new GraphExtractionTaskId(1L));
        task.setExecutionStatus(executionStatus);
        task.setDisposition(disposition);
        return task;
    }
}
