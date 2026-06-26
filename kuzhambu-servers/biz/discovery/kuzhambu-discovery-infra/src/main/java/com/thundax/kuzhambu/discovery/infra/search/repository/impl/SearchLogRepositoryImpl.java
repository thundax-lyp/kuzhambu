package com.thundax.kuzhambu.discovery.infra.search.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchLog;
import com.thundax.kuzhambu.discovery.domain.search.repository.SearchLogRepository;
import com.thundax.kuzhambu.discovery.infra.search.persistence.assembler.SearchLogPersistenceAssembler;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.SearchLogDO;
import com.thundax.kuzhambu.discovery.infra.search.persistence.mapper.SearchLogMapper;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SearchLogRepositoryImpl implements SearchLogRepository {

    private final SearchLogMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public SearchLogRepositoryImpl(SearchLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SearchLog getBySearchLogId(String searchLogId) {
        return SearchLogPersistenceAssembler.toDomain(mapper.selectOne(
                new QueryWrapper<SearchLogDO>().eq("search_log_id", searchLogId).last("limit 1")));
    }

    @Override
    public Long save(SearchLog entity) {
        SearchLogDO dataObject = SearchLogPersistenceAssembler.toObject(entity);
        long nextId = idGenerator.nextId().value();
        dataObject.setId(nextId);
        if (StringUtils.isBlank(dataObject.getSearchLogId())) {
            dataObject.setSearchLogId(String.valueOf(nextId));
        }
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public List<SearchLog> listByCreatedAtRange(Date createdAtStart, Date createdAtEnd) {
        return SearchLogPersistenceAssembler.toDomainList(mapper.selectByCreatedAtRange(createdAtStart, createdAtEnd));
    }

    @Override
    public PageResult<SearchLog> page(
            String queryText, String intentType, String searchStatus, String operatorId, int pageNo, int pageSize) {
        Page<SearchLogDO> page = new Page<>(pageNo, pageSize);
        IPage<SearchLogDO> dataObjectPage =
                mapper.selectPage(page, buildPageWrapper(queryText, intentType, searchStatus, operatorId));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                SearchLogPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    private QueryWrapper<SearchLogDO> buildPageWrapper(
            String queryText, String intentType, String searchStatus, String operatorId) {
        QueryWrapper<SearchLogDO> wrapper = new QueryWrapper<>();
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
