package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchLog;

public interface SearchLogRepository {

    SearchLog getBySearchLogId(String searchLogId);

    Long save(SearchLog entity);

    PageResult<SearchLog> page(
            String queryText, String intentType, String searchStatus, String operatorId, int pageNo, int pageSize);
}
