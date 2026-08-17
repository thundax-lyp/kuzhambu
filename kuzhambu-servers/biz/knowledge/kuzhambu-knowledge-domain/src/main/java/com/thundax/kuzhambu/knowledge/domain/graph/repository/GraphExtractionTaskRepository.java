package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.time.Instant;
import java.util.List;

public interface GraphExtractionTaskRepository {
    GraphExtractionTask getById(GraphExtractionTaskId id);

    List<GraphExtractionTask> listByMaterialId(Long materialId);

    List<GraphExtractionTask> listByBatchId(String batchId);

    List<GraphExtractionTask> listPurgeableBefore(Instant deadline, int limit);

    GraphExtractionTaskId insert(GraphExtractionTask task);

    int updateIfLockVersion(GraphExtractionTask task, long expectedLockVersion);

    int deleteById(GraphExtractionTaskId id);
}
