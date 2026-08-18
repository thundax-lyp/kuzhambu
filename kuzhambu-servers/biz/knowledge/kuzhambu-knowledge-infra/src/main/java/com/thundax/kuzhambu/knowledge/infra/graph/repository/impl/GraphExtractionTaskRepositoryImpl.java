package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.GraphExtractionTaskWithMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphExtractionTaskPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphExtractionTaskMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.projection.GraphExtractionTaskWithMaterialProjection;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphExtractionTaskRepositoryImpl implements GraphExtractionTaskRepository {
    private final GraphExtractionTaskMapper mapper;

    public GraphExtractionTaskRepositoryImpl(GraphExtractionTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphExtractionTask getById(GraphExtractionTaskId id) {
        return GraphExtractionTaskPersistenceAssembler.toDomain(
                mapper.selectById(GraphExtractionTaskIdCodec.toValue(id)));
    }

    @Override
    public GraphExtractionTask getByIdempotencyKey(String idempotencyKey) {
        return GraphExtractionTaskPersistenceAssembler.toDomain(mapper.selectByIdempotencyKey(idempotencyKey));
    }

    @Override
    public List<GraphExtractionTask> listByMaterialId(Long materialId) {
        return mapper.selectByMaterialId(materialId).stream()
                .map(GraphExtractionTaskPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphExtractionTask> listLatestByMaterialIds(List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectLatestByMaterialIds(materialIds).stream()
                .map(GraphExtractionTaskPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphExtractionTask> listByBatchId(String batchId) {
        return mapper.selectByBatchId(batchId).stream()
                .map(GraphExtractionTaskPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<ContentRef> listContentRefsByTaskState(
            GraphExtractionExecutionStatus executionStatus, GraphExtractionDisposition disposition) {
        String statusValue = executionStatus == null ? null : executionStatus.value();
        String dispositionValue = disposition == null ? null : disposition.value();
        return mapper.selectContentRefsByTaskState(statusValue, dispositionValue).stream()
                .map(row -> ContentRefCodec.toDomain(row.getContentType(), row.getContentRefId()))
                .toList();
    }

    @Override
    public PageResult<GraphExtractionTask> page(
            List<ContentRef> contentRefs,
            String batchId,
            GraphExtractionExecutionStatus executionStatus,
            GraphExtractionDisposition disposition,
            int pageNo,
            int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        List<GraphExtractionTaskDO> refs = contentRefs == null
                ? List.of()
                : contentRefs.stream()
                        .map(ref -> {
                            GraphExtractionTaskDO row = new GraphExtractionTaskDO();
                            row.setContentType(ContentRefCodec.toContentType(ref));
                            row.setContentRefId(ContentRefCodec.toValue(ref));
                            return row;
                        })
                        .toList();
        String statusValue = executionStatus == null ? null : executionStatus.value();
        String dispositionValue = disposition == null ? null : disposition.value();
        long total = mapper.countTasks(refs, batchId, statusValue, dispositionValue);
        List<GraphExtractionTask> records = mapper
                .pageTasks(
                        refs,
                        batchId,
                        statusValue,
                        dispositionValue,
                        (effectivePageNo - 1) * effectivePageSize,
                        effectivePageSize)
                .stream()
                .map(GraphExtractionTaskPersistenceAssembler::toDomain)
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public PageResult<GraphExtractionTaskWithMaterial> listWithMaterialTitle(
            List<ContentRef> contentRefs,
            String batchId,
            GraphExtractionExecutionStatus executionStatus,
            GraphExtractionDisposition disposition,
            int pageNo,
            int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        List<GraphExtractionTaskDO> refs = contentRefs == null
                ? List.of()
                : contentRefs.stream()
                        .map(ref -> {
                            GraphExtractionTaskDO row = new GraphExtractionTaskDO();
                            row.setContentType(ContentRefCodec.toContentType(ref));
                            row.setContentRefId(ContentRefCodec.toValue(ref));
                            return row;
                        })
                        .toList();
        String statusValue = executionStatus == null ? null : executionStatus.value();
        String dispositionValue = disposition == null ? null : disposition.value();
        long total = mapper.countTasks(refs, batchId, statusValue, dispositionValue);
        List<GraphExtractionTaskWithMaterial> records = mapper
                .pageTasksWithMaterialTitle(
                        refs,
                        batchId,
                        statusValue,
                        dispositionValue,
                        (effectivePageNo - 1) * effectivePageSize,
                        effectivePageSize)
                .stream()
                .map((GraphExtractionTaskWithMaterialProjection row) -> new GraphExtractionTaskWithMaterial(
                        GraphExtractionTaskPersistenceAssembler.toDomain(row), row.getMaterialTitle()))
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public List<GraphExtractionTask> listPurgeableBefore(Instant deadline, int limit) {
        return mapper.selectPurgeableBefore(deadline, limit <= 0 ? 100 : limit).stream()
                .map(GraphExtractionTaskPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphExtractionTaskId insert(GraphExtractionTask task) {
        GraphExtractionTaskDO dataObject = GraphExtractionTaskPersistenceAssembler.toObject(task);
        mapper.insert(dataObject);
        return GraphExtractionTaskIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateIfLockVersion(GraphExtractionTask task, long expectedLockVersion) {
        return mapper.updateIfLockVersion(GraphExtractionTaskPersistenceAssembler.toObject(task), expectedLockVersion);
    }

    @Override
    public int deleteById(GraphExtractionTaskId id) {
        return mapper.deleteById(GraphExtractionTaskIdCodec.toValue(id));
    }
}
