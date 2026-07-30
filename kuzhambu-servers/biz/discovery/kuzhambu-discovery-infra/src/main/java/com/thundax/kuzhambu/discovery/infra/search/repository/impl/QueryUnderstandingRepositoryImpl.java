package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.discovery.domain.search.codec.QueryUnderstandingIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.QueryUnderstandingId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
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
    public QueryUnderstanding getBySearchEventId(SearchEventId searchEventId) {
        return QueryUnderstandingPersistenceAssembler.toDomain(mapper.selectOne(new QueryWrapper<QueryUnderstandingDO>()
                .eq("search_event_id", SearchEventIdCodec.toValue(searchEventId))
                .last("limit 1")));
    }

    @Override
    public QueryUnderstandingId save(QueryUnderstanding entity) {
        QueryUnderstandingDO dataObject = QueryUnderstandingPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return QueryUnderstandingIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(QueryUnderstanding entity) {
        return mapper.updateById(QueryUnderstandingPersistenceAssembler.toObject(entity));
    }
}
