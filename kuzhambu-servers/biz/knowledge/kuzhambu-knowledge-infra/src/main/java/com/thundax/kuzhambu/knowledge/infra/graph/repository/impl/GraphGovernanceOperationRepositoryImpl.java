package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphGovernanceOperationIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphGovernanceOperation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphGovernanceOperationId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphGovernanceOperationRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphGovernanceOperationDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphGovernanceOperationMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class GraphGovernanceOperationRepositoryImpl implements GraphGovernanceOperationRepository {

    private final GraphGovernanceOperationMapper mapper;

    public GraphGovernanceOperationRepositoryImpl(GraphGovernanceOperationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphGovernanceOperation getById(GraphGovernanceOperationId id) {
        return GraphPersistenceAssembler.toDomain(mapper.selectById(GraphGovernanceOperationIdCodec.toValue(id)));
    }

    @Override
    public List<GraphGovernanceOperation> listByTarget(String targetType, Long targetId) {
        QueryWrapper<GraphGovernanceOperationDO> w = new QueryWrapper<>();
        return mapper
                .selectList(w.eq("target_type", targetType)
                        .eq("target_id", targetId)
                        .orderByDesc("operated_at")
                        .orderByDesc("id"))
                .stream()
                .map(GraphPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public GraphGovernanceOperationId insert(GraphGovernanceOperation operation) {
        GraphGovernanceOperationDO dataObject = GraphPersistenceAssembler.toObject(operation);
        mapper.insert(dataObject);
        return GraphGovernanceOperationIdCodec.toDomain(dataObject.getId());
    }
}
