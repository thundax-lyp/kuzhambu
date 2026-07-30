package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;
import com.thundax.kuzhambu.discovery.domain.search.repository.QueryUnderstandingRepository;
import com.thundax.kuzhambu.discovery.infra.search.persistence.assembler.QueryUnderstandingPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.QueryUnderstandingDO;
import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.QueryUnderstandingMapper;
import org.springframework.stereotype.Repository;

@Repository
public class QueryUnderstandingRepositoryImpl implements QueryUnderstandingRepository {

    private final QueryUnderstandingMapper mapper;

    public QueryUnderstandingRepositoryImpl(QueryUnderstandingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public QueryUnderstanding getBySearchEventId(Long searchEventId) {
        return QueryUnderstandingPersistenceAssembler.toDomain(mapper.selectOne(new QueryWrapper<QueryUnderstandingDO>()
                .eq("search_event_id", searchEventId)
                .last("limit 1")));
    }

    @Override
    public Long save(QueryUnderstanding entity) {
        QueryUnderstandingDO dataObject = QueryUnderstandingPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(QueryUnderstanding entity) {
        return mapper.updateById(QueryUnderstandingPersistenceAssembler.toObject(entity));
    }
}
