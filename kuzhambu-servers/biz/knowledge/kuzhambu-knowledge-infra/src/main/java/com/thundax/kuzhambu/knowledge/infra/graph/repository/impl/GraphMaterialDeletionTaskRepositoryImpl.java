package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialDeletionTaskIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialDeletionTaskRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDeletionTaskDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialDeletionTaskMapper;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialDeletionTaskRepositoryImpl implements GraphMaterialDeletionTaskRepository {
    private final GraphMaterialDeletionTaskMapper mapper;

    public GraphMaterialDeletionTaskRepositoryImpl(GraphMaterialDeletionTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphMaterialDeletionTask getById(GraphMaterialDeletionTaskId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphMaterialDeletionTaskIdCodec.toValue(id)));
    }

    @Override
    public GraphMaterialDeletionTask getByIdempotencyKey(String idempotencyKey) {
        QueryWrapper<GraphMaterialDeletionTaskDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(mapper.selectOne(w.eq("idempotency_key", idempotencyKey)));
    }

    @Override
    public PageResult<GraphMaterialDeletionTask> page(GraphMaterialDeletionStatus status, int pageNo, int pageSize) {
        int effectivePageNo = pageNo <= 0 ? 1 : pageNo;
        int effectivePageSize = pageSize <= 0 ? 10 : pageSize;
        long total = mapper.selectCount(taskQuery(status));
        QueryWrapper<GraphMaterialDeletionTaskDO> pageWrapper = taskQuery(status)
                .orderByDesc("requested_at")
                .orderByDesc("id")
                .last("limit " + offset(effectivePageNo, effectivePageSize) + ", " + effectivePageSize);
        List<GraphMaterialDeletionTask> records = mapper.selectList(pageWrapper).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
        return PageResult.of(effectivePageNo, effectivePageSize, total, records);
    }

    @Override
    public List<GraphMaterialDeletionTask> listByStatus(GraphMaterialDeletionStatus status, int limit) {
        QueryWrapper<GraphMaterialDeletionTaskDO> w = new QueryWrapper<>();
        int effectiveLimit = limit <= 0 ? 20 : limit;
        return mapper
                .selectList(w.eq("status", status.value())
                        .orderByAsc("requested_at")
                        .orderByAsc("id")
                        .last("limit " + effectiveLimit))
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphMaterialDeletionTaskId insert(GraphMaterialDeletionTask task) {
        GraphMaterialDeletionTaskDO dataObject = GraphPersistenceAssembler.toObject(task);
        try {
            mapper.insert(dataObject);
            return GraphMaterialDeletionTaskIdCodec.toDomain(dataObject.getId());
        } catch (DuplicateKeyException ex) {
            GraphMaterialDeletionTask existing = getByIdempotencyKey(task.getIdempotencyKey());
            if (existing != null) {
                return existing.getId();
            }
            throw ex;
        }
    }

    @Override
    public GraphMaterialDeletionTask updateIfLockVersion(GraphMaterialDeletionTask task, long expectedLockVersion) {
        int updated = mapper.updateIfLockVersion(GraphPersistenceAssembler.toObject(task), expectedLockVersion);
        if (updated != 1) {
            throw GraphMaterialDeletionChange.lockConflict();
        }
        return getById(task.getId());
    }

    private QueryWrapper<GraphMaterialDeletionTaskDO> taskQuery(GraphMaterialDeletionStatus status) {
        QueryWrapper<GraphMaterialDeletionTaskDO> w = new QueryWrapper<>();
        if (status != null) {
            w.eq("status", status.value());
        }
        return w;
    }

    private int offset(int pageNo, int pageSize) {
        return (pageNo - 1) * pageSize;
    }
}
