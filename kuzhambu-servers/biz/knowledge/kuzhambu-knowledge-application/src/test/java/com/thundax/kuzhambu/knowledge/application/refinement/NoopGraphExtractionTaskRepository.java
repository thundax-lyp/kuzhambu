package com.thundax.kuzhambu.knowledge.application.refinement;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import java.util.List;

final class NoopGraphExtractionTaskRepository implements GraphExtractionTaskRepository {

    @Override
    public GraphExtractionTask getByTaskId(GraphExtractionTaskId taskId) {
        return null;
    }

    @Override
    public GraphExtractionTaskId save(GraphExtractionTask entity) {
        return null;
    }

    @Override
    public int update(GraphExtractionTask entity) {
        return 0;
    }

    @Override
    public List<GraphExtractionTask> listByBatchJobId(Long batchJobId) {
        return List.of();
    }

    @Override
    public PageResult<GraphExtractionTask> page(
            String taskType,
            Long batchJobId,
            String triggerSource,
            String status,
            String sourceContentType,
            Long sourceContentId,
            int pageNo,
            int pageSize) {
        return PageResult.of(pageNo, pageSize, 0, List.of());
    }
}
