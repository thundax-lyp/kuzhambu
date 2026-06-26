package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public interface GraphExtractionTaskRepository {

    GraphExtractionTask getByTaskId(GraphExtractionTaskId taskId);

    GraphExtractionTaskId save(GraphExtractionTask entity);

    int update(GraphExtractionTask entity);

    java.util.List<GraphExtractionTask> listByBatchJobId(Long batchJobId);

    PageResult<GraphExtractionTask> page(
            String taskType, String status, String sourceContentType, Long sourceContentId, int pageNo, int pageSize);
}
