package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionBatchJobId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionSourceContentId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;

public interface GraphExtractionTaskRepository {

    GraphExtractionTask getByTaskId(GraphExtractionTaskId taskId);

    GraphExtractionTaskId save(GraphExtractionTask entity);

    int update(GraphExtractionTask entity);

    java.util.List<GraphExtractionTask> listByBatchJobId(GraphExtractionBatchJobId batchJobId);

    PageResult<GraphExtractionTask> page(
            String taskType,
            GraphExtractionBatchJobId batchJobId,
            String triggerSource,
            String status,
            String sourceContentType,
            GraphExtractionSourceContentId sourceContentId,
            int pageNo,
            int pageSize);
}
