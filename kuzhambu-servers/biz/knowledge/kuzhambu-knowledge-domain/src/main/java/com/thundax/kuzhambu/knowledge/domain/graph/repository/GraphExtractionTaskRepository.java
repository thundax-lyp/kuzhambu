package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.GraphExtractionTaskWithMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.time.Instant;
import java.util.List;

public interface GraphExtractionTaskRepository {
    GraphExtractionTask getById(GraphExtractionTaskId id);

    GraphExtractionTask getByIdempotencyKey(String idempotencyKey);

    List<GraphExtractionTask> listByMaterialId(Long materialId);

    List<GraphExtractionTask> listLatestByMaterialIds(List<Long> materialIds);

    List<GraphExtractionTask> listByBatchId(String batchId);

    List<ContentRef> listContentRefsByTaskState(
            GraphExtractionExecutionStatus executionStatus, GraphExtractionDisposition disposition);

    PageResult<GraphExtractionTask> page(
            List<ContentRef> contentRefs,
            String batchId,
            GraphExtractionExecutionStatus executionStatus,
            GraphExtractionDisposition disposition,
            int pageNo,
            int pageSize);

    PageResult<GraphExtractionTaskWithMaterial> listWithMaterialTitle(
            List<ContentRef> contentRefs,
            String batchId,
            GraphExtractionExecutionStatus executionStatus,
            GraphExtractionDisposition disposition,
            int pageNo,
            int pageSize);

    List<GraphExtractionTask> listPurgeableBefore(Instant deadline, int limit);

    GraphExtractionTaskId insert(GraphExtractionTask task);

    int updateIfLockVersion(GraphExtractionTask task, long expectedLockVersion);

    int deleteById(GraphExtractionTaskId id);
}
