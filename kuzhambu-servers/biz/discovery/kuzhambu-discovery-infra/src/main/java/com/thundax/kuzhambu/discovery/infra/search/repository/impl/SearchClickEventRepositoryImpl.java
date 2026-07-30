package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.thundax.kuzhambu.discovery.domain.search.codec.SearchClickEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClickEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchClickEventId;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickEventRepository;
import com.thundax.kuzhambu.discovery.infra.search.persistence.assembler.SearchClickEventPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchClickEventDO;
import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.SearchClickEventMapper;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class SearchClickEventRepositoryImpl implements SearchClickEventRepository {

    private final SearchClickEventMapper mapper;

    public SearchClickEventRepositoryImpl(SearchClickEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SearchClickEvent getById(SearchClickEventId id) {
        return SearchClickEventPersistenceAssembler.toDomain(mapper.selectById(SearchClickEventIdCodec.toValue(id)));
    }

    @Override
    public SearchClickEventId save(SearchClickEvent entity) {
        SearchClickEventDO dataObject = SearchClickEventPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return SearchClickEventIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public long countByCreatedAtRange(Instant createdAtStart, Instant createdAtEnd) {
        Long count = mapper.countByCreatedAtRange(createdAtStart, createdAtEnd);
        return count == null ? 0L : count;
    }
}
