package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.search.codec.SearchEventIdCodec;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchEventRepository;
import com.thundax.kuzhambu.discovery.infra.search.persistence.assembler.SearchEventPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchEventDO;
import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.SearchEventMapper;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SearchEventRepositoryImpl implements SearchEventRepository {

    private final SearchEventMapper mapper;

    public SearchEventRepositoryImpl(SearchEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SearchEvent getById(SearchEventId id) {
        return SearchEventPersistenceAssembler.toDomain(mapper.selectById(SearchEventIdCodec.toValue(id)));
    }

    @Override
    public SearchEventId save(SearchEvent entity) {
        SearchEventDO dataObject = SearchEventPersistenceAssembler.toObject(entity);
        mapper.insert(dataObject);
        return SearchEventIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public List<SearchEvent> listByCreatedAtRange(Instant createdAtStart, Instant createdAtEnd) {
        return SearchEventPersistenceAssembler.toDomainList(
                mapper.selectByCreatedAtRange(createdAtStart, createdAtEnd));
    }

    @Override
    public PageResult<SearchEvent> page(
            String queryText, String intentType, String searchStatus, String operatorId, int pageNo, int pageSize) {
        Page<SearchEventDO> page = new Page<>(pageNo, pageSize);
        IPage<SearchEventDO> dataObjectPage =
                mapper.selectPage(page, buildPageWrapper(queryText, intentType, searchStatus, operatorId));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                SearchEventPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    private QueryWrapper<SearchEventDO> buildPageWrapper(
            String queryText, String intentType, String searchStatus, String operatorId) {
        QueryWrapper<SearchEventDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(queryText)) {
            wrapper.like("query_text", queryText);
        }
        if (StringUtils.isNotBlank(intentType)) {
            wrapper.eq("intent_type", intentType);
        }
        if (StringUtils.isNotBlank(searchStatus)) {
            wrapper.eq("search_status", searchStatus);
        }
        if (StringUtils.isNotBlank(operatorId)) {
            wrapper.eq("operator_id", operatorId);
        }
        wrapper.orderByDesc("created_at");
        return wrapper;
    }
}
