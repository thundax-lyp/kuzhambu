package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphNodeKeyCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedNodeMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishedNodeRepositoryImpl implements GraphPublishedNodeRepository {
    private final GraphPublishedNodeMapper mapper;

    public GraphPublishedNodeRepositoryImpl(GraphPublishedNodeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphPublishedNode getById(GraphPublishedNodeId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphPublishedNodeIdCodec.toValue(id)));
    }

    @Override
    public GraphPublishedNode getByNodeKey(GraphNodeKey key) {
        QueryWrapper<GraphPublishedNodeDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(mapper.selectOne(w.eq("node_key", GraphNodeKeyCodec.toValue(key))));
    }

    @Override
    public List<GraphPublishedNode> listByIds(List<GraphPublishedNodeId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return mapper
                .selectBatchIds(
                        ids.stream().map(GraphPublishedNodeIdCodec::toValue).toList())
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<GraphPublishedNode> listRecentlyUpdated(int limit) {
        QueryWrapper<GraphPublishedNodeDO> w = new QueryWrapper<>();
        return mapper
                .selectList(w.eq("status", "ACTIVE")
                        .orderByDesc("modified_at")
                        .orderByDesc("id")
                        .last("LIMIT " + limit))
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public int insert(GraphPublishedNode node) {
        return mapper.insert(GraphPersistenceAssembler.toObject(node));
    }

    @Override
    public int update(GraphPublishedNode node) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(node));
    }
}
