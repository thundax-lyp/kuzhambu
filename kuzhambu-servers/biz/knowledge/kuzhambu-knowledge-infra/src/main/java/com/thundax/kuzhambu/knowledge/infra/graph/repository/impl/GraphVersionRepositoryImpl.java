package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler.GraphVersionPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphVersionDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphVersionMapper;
import org.springframework.stereotype.Repository;

@Repository
public class GraphVersionRepositoryImpl implements GraphVersionRepository {

    private final GraphVersionMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public GraphVersionRepositoryImpl(GraphVersionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GraphVersion findLatest(String taskType, String sourceContentType, Long sourceContentId) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("task_type", taskType)
                .eq("source_content_type", sourceContentType)
                .eq("source_content_id", sourceContentId)
                .orderByDesc("version_no")
                .last("limit 1");
        return GraphVersionPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public GraphVersion getByTaskCandidate(GraphExtractionTaskId taskId, Long candidateId) {
        QueryWrapper<GraphVersionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId == null ? null : taskId.value()).eq("candidate_id", candidateId);
        return GraphVersionPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public Long save(GraphVersion entity) {
        GraphVersionDO dataObject = GraphVersionPersistenceAssembler.toObject(entity);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        if (dataObject.getVersionId() == null) {
            dataObject.setVersionId(dataObject.getId());
        }
        mapper.insert(dataObject);
        return dataObject.getVersionId();
    }
}
