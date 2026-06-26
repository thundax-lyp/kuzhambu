package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchLog;
import java.util.Date;
import java.util.List;

public interface SearchLogRepository {

    SearchLog getBySearchLogId(String searchLogId);

    Long save(SearchLog entity);

    List<SearchLog> listByCreatedAtRange(Date createdAtStart, Date createdAtEnd);

    PageResult<SearchLog> page(
            String queryText, String intentType, String searchStatus, String operatorId, int pageNo, int pageSize);
}
