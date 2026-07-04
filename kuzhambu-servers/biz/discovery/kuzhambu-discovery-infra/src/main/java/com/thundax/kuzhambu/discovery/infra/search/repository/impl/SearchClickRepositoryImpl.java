package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClick;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchClickRepository;
import com.thundax.kuzhambu.discovery.infra.search.persistence.assembler.SearchClickPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchClickDO;
import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.SearchClickMapper;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SearchClickRepositoryImpl implements SearchClickRepository {

    private final SearchClickMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public SearchClickRepositoryImpl(SearchClickMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SearchClick getBySearchClickId(String searchClickId) {
        return SearchClickPersistenceAssembler.toDomain(mapper.selectOne(new QueryWrapper<SearchClickDO>()
                .eq("search_click_id", searchClickId)
                .last("limit 1")));
    }

    @Override
    public Long save(SearchClick entity) {
        SearchClickDO dataObject = SearchClickPersistenceAssembler.toObject(entity);
        long nextId = idGenerator.nextId().value();
        dataObject.setId(nextId);
        if (StringUtils.isBlank(dataObject.getSearchClickId())) {
            dataObject.setSearchClickId(String.valueOf(nextId));
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
