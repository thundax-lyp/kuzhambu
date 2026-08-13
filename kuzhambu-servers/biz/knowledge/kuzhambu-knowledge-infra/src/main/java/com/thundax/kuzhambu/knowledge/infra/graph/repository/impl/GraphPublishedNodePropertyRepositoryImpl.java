package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodePropertyIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodePropertyId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodePropertyRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodePropertyDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphPublishedNodePropertyMapper;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class GraphPublishedNodePropertyRepositoryImpl implements GraphPublishedNodePropertyRepository {
    private final GraphPublishedNodePropertyMapper mapper;

    public GraphPublishedNodePropertyRepositoryImpl(GraphPublishedNodePropertyMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphPublishedNodeProperty getById(GraphPublishedNodePropertyId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphPublishedNodePropertyIdCodec.toValue(id)));
    }

    @Override
    public List<GraphPublishedNodeProperty> listByPublishedNodeId(GraphPublishedNodeId id) {
        QueryWrapper<GraphPublishedNodePropertyDO> w = new QueryWrapper<>();
        return mapper.selectList(w.eq("published_node_id", GraphPublishedNodeIdCodec.toValue(id))).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphPublishedNodePropertyId insert(GraphPublishedNodeProperty property) {
        GraphPublishedNodePropertyDO dataObject = GraphPersistenceAssembler.toObject(property);
        try {
            mapper.insert(dataObject);
            return GraphPublishedNodePropertyIdCodec.toDomain(dataObject.getId());
        } catch (DuplicateKeyException ex) {
            GraphPublishedNodeProperty existing = getByUniqueKey(property);
            if (existing != null) {
                return existing.getId();
            }
            throw ex;
        }
    }

    @Override
    public void batchInsert(List<GraphPublishedNodeProperty> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        properties.forEach(this::insert);
    }

    @Override
    public int update(GraphPublishedNodeProperty property) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(property));
    }

    @Override
    public int deleteById(GraphPublishedNodePropertyId id) {
        return mapper.deleteById(GraphPublishedNodePropertyIdCodec.toValue(id));
    }

    @Override
    public int deleteByPublishedNodeId(GraphPublishedNodeId id) {
        QueryWrapper<GraphPublishedNodePropertyDO> w = new QueryWrapper<>();
        return mapper.delete(w.eq("published_node_id", GraphPublishedNodeIdCodec.toValue(id)));
    }

    @Override
    public int deleteByPublishedNodeIds(List<GraphPublishedNodeId> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        QueryWrapper<GraphPublishedNodePropertyDO> w = new QueryWrapper<>();
        return mapper.delete(w.in(
                "published_node_id",
                ids.stream().map(GraphPublishedNodeIdCodec::toValue).toList()));
    }

    private GraphPublishedNodeProperty getByUniqueKey(GraphPublishedNodeProperty property) {
        QueryWrapper<GraphPublishedNodePropertyDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(mapper.selectOne(
                w.eq("published_node_id", GraphPublishedNodeIdCodec.toValue(property.getPublishedNodeId()))
                        .eq("property_name", property.getPropertyKey())
                        .eq("value", property.getValue())));
    }
}
