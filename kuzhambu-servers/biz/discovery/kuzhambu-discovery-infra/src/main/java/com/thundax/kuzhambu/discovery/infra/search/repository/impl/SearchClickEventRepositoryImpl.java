package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClickEvent;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickEventRepository;
import com.thundax.kuzhambu.discovery.infra.search.persistence.assembler.SearchClickEventPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchClickEventDO;
import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.SearchClickEventMapper;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SearchClickEventRepositoryImpl implements SearchClickEventRepository {

    private final SearchClickEventMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public SearchClickEventRepositoryImpl(SearchClickEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SearchClickEvent getBySearchClickEventId(String searchClickEventId) {
        return SearchClickEventPersistenceAssembler.toDomain(mapper.selectOne(new QueryWrapper<SearchClickEventDO>()
                .eq("search_click_event_id", searchClickEventId)
                .last("limit 1")));
    }

    @Override
    public Long save(SearchClickEvent entity) {
        SearchClickEventDO dataObject = SearchClickEventPersistenceAssembler.toObject(entity);
        long nextId = idGenerator.nextId().value();
        dataObject.setId(nextId);
        if (StringUtils.isBlank(dataObject.getSearchClickEventId())) {
            dataObject.setSearchClickEventId(String.valueOf(nextId));
        }
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd) {
        Long count = mapper.countByCreatedAtRange(createdAtStart, createdAtEnd);
        return count == null ? 0L : count;
    }
}
