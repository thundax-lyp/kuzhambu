package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;
import com.thundax.kuzhambu.discovery.domain.search.repository.QueryUnderstandingRepository;
import com.thundax.kuzhambu.discovery.infra.search.persistence.assembler.QueryUnderstandingPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.QueryUnderstandingDO;
import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.QueryUnderstandingMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class QueryUnderstandingRepositoryImpl implements QueryUnderstandingRepository {

    private final QueryUnderstandingMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public QueryUnderstandingRepositoryImpl(QueryUnderstandingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public QueryUnderstanding getBySearchEventId(String searchEventId) {
        return QueryUnderstandingPersistenceAssembler.toDomain(mapper.selectOne(new QueryWrapper<QueryUnderstandingDO>()
                .eq("search_event_id", searchEventId)
                .last("limit 1")));
    }

    @Override
    public Long save(QueryUnderstanding entity) {
        QueryUnderstandingDO dataObject = QueryUnderstandingPersistenceAssembler.toObject(entity);
        long nextId = idGenerator.nextId().value();
        dataObject.setId(nextId);
        if (StringUtils.isBlank(dataObject.getQueryUnderstandingId())) {
            dataObject.setQueryUnderstandingId(String.valueOf(nextId));
        }
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(QueryUnderstanding entity) {
        return mapper.updateById(QueryUnderstandingPersistenceAssembler.toObject(entity));
    }
}
