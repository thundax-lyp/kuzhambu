package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgePropertyIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgePropertyId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgePropertyRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgePropertyDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedEdgePropertyMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishedEdgePropertyRepositoryImpl implements GraphPublishedEdgePropertyRepository {
    private final GraphPublishedEdgePropertyMapper mapper;

    public GraphPublishedEdgePropertyRepositoryImpl(GraphPublishedEdgePropertyMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphPublishedEdgeProperty getById(GraphPublishedEdgePropertyId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphPublishedEdgePropertyIdCodec.toValue(id)));
    }

    @Override
    public List<GraphPublishedEdgeProperty> listByPublishedEdgeId(GraphPublishedEdgeId id) {
        QueryWrapper<GraphPublishedEdgePropertyDO> w = new QueryWrapper<>();
        return mapper.selectList(w.eq("published_edge_id", GraphPublishedEdgeIdCodec.toValue(id))).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public int insert(GraphPublishedEdgeProperty property) {
        return mapper.insert(GraphPersistenceAssembler.toObject(property));
    }

    @Override
    public int update(GraphPublishedEdgeProperty property) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(property));
    }

    @Override
    public int deleteById(GraphPublishedEdgePropertyId id) {
        return mapper.deleteById(GraphPublishedEdgePropertyIdCodec.toValue(id));
    }
}
