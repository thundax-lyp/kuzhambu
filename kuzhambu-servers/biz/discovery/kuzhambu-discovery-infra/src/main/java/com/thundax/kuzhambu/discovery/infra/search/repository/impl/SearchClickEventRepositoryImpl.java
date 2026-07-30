package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClickEvent;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickEventRepository;
import com.thundax.kuzhambu.discovery.infra.search.persistence.assembler.SearchClickEventPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchClickEventDO;
import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.SearchClickEventMapper;
import java.util.Date;
import org.springframework.stereotype.Repository;

@Repository
public class SearchClickEventRepositoryImpl implements SearchClickEventRepository {

    private final SearchClickEventMapper mapper;

    public SearchClickEventRepositoryImpl(SearchClickEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SearchClickEvent getById(Long id) {
        return SearchClickEventPersistenceAssembler.toDomain(mapper.selectById(id));
    }

    @Override
    public Long save(SearchClickEvent entity) {
        SearchClickEventDO dataObject = SearchClickEventPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd) {
        Long count = mapper.countByCreatedAtRange(createdAtStart, createdAtEnd);
        return count == null ? 0L : count;
    }
}
