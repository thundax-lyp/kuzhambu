package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialEventIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEventRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialEventDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialEventMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphMaterialEventRepositoryImpl implements GraphMaterialEventRepository {
    private final GraphMaterialEventMapper mapper;

    public GraphMaterialEventRepositoryImpl(GraphMaterialEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphMaterialEvent getById(GraphMaterialEventId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphMaterialEventIdCodec.toValue(id)));
    }

    @Override
    public GraphMaterialEvent getByMaterialRef(ContentRef ref) {
        QueryWrapper<GraphMaterialEventDO> w = new QueryWrapper<>();
        return GraphPersistenceAssembler.toDomain(
                mapper.selectOne(w.eq("content_type", ContentRefCodec.toContentType(ref))
                        .eq("content_ref_id", ContentRefCodec.toValue(ref))));
    }

    @Override
    public List<GraphMaterialEvent> listByStatus(GraphMaterialEventStatus status) {
        QueryWrapper<GraphMaterialEventDO> w = new QueryWrapper<>();
        return mapper.selectList(w.eq("status", status.value()).orderByAsc("changed_at")).stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public int insert(GraphMaterialEvent event) {
        return mapper.insert(GraphPersistenceAssembler.toObject(event));
    }

    @Override
    public int update(GraphMaterialEvent event) {
        return mapper.updateById(GraphPersistenceAssembler.toObject(event));
    }
}
