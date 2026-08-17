package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphExtractionTaskPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphExtractionTaskMapper;
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
    public List<GraphExtractionTask> listByMaterialId(Long materialId) {
        return mapper.selectByMaterialId(materialId).stream()
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
