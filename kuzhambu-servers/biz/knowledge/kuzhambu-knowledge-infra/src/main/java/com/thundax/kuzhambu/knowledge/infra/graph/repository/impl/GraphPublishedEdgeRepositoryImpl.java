package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphEdgeKeyCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedEdgeMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishedEdgeRepositoryImpl implements GraphPublishedEdgeRepository {
    private final GraphPublishedEdgeMapper mapper;

    public GraphPublishedEdgeRepositoryImpl(GraphPublishedEdgeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphPublishedEdge getById(GraphPublishedEdgeId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphPublishedEdgeIdCodec.toValue(id)));
    }

    @Override
    public GraphPublishedEdge getByEdgeKey(GraphEdgeKey key) {
        QueryWrapper<GraphPublishedEdgeDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(mapper.selectOne(w.eq("edge_key", GraphEdgeKeyCodec.toValue(key))));
    }

    @Override
    public List<GraphPublishedEdge> listByNodeIds(List<GraphPublishedNodeId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Long> values = ids.stream().map(GraphPublishedNodeIdCodec::toValue).toList();
        QueryWrapper<GraphPublishedEdgeDO> w = new QueryWrapper<>();
        return mapper
                .selectList(w.nested(
                        q -> q.in("source_published_node_id", values).or().in("target_published_node_id", values)))
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphPublishedEdgeSlice listIncidentEdges(
            List<GraphPublishedNodeId> ids, GraphPublishedEdgeId after, int limit) {
        List<GraphPublishedEdge> edges = listByNodeIds(ids).stream()
                .filter(v -> after == null || v.getId().value() > after.value())
                .limit(limit + 1L)
                .toList();
        boolean truncated = edges.size() > limit;
        List<GraphPublishedEdge> page = truncated ? edges.subList(0, limit) : edges;
        return new GraphPublishedEdgeSlice(
                page, truncated ? page.get(page.size() - 1).getId() : null, truncated);
    }

    @Override
    public int insert(GraphPublishedEdge edge) {
        return mapper.insert(GraphPersistenceAssembler.toObject(edge));
    }

    @Override
    public int update(GraphPublishedEdge edge) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(edge));
    }
}
