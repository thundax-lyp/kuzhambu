package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphManualSourceIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphManualSource;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphManualSourceId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphManualSourceRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphManualSourceDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphManualSourceMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphManualSourceRepositoryImpl implements GraphManualSourceRepository {

    private final GraphManualSourceMapper mapper;

    public GraphManualSourceRepositoryImpl(GraphManualSourceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphManualSource getById(GraphManualSourceId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphManualSourceIdCodec.toValue(id)));
    }

    @Override
    public List<GraphManualSource> listByTarget(String targetType, Long targetId) {
        QueryWrapper<GraphManualSourceDO> w = new QueryWrapper<>();
        return mapper
                .selectList(w.eq("target_type", targetType)
                        .eq("target_id", targetId)
                        .orderByDesc("recorded_at")
                        .orderByDesc("id"))
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphManualSourceId insert(GraphManualSource source) {
        GraphManualSourceDO dataObject = GraphPersistenceAssembler.toObject(source);
        mapper.insert(dataObject);
        return GraphManualSourceIdCodec.toDomain(dataObject.getId());
    }
}
