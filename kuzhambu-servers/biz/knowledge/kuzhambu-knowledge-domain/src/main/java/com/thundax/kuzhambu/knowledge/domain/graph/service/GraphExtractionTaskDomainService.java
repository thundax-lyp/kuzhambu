package com.thundax.kuzhambu.knowledge.domain.graph.service;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.time.Instant;

public class GraphExtractionTaskDomainService {

    public void retry(GraphExtractionTask task) {
        task.retry();
    }

    public void cancel(GraphExtractionTask task, Instant completedAt) {
        task.cancel(completedAt);
    }

    public void adopt(
            GraphExtractionTask task, GraphExtractionDisposition disposition, Instant disposedAt, Instant purgeAfter) {
        task.adopt(disposition, disposedAt, purgeAfter);
    }

    public void discard(GraphExtractionTask task, Instant disposedAt, Instant purgeAfter) {
        task.discard(disposedAt, purgeAfter);
    }

    public void supersede(
            GraphExtractionTask task, GraphExtractionTaskId nextTaskId, Instant disposedAt, Instant purgeAfter) {
        task.supersede(nextTaskId, disposedAt, purgeAfter);
    }

    public void regenerate(GraphExtractionTask previousTask, GraphExtractionTask nextTask) {
        if (previousTask == null || previousTask.getId() == null || nextTask == null) {
            throw new IllegalArgumentException("Previous and next graph extraction tasks are required");
        }
        nextTask.setRegeneratedFromTaskId(previousTask.getId());
    }
}
