package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;
import java.util.Date;
import java.util.List;

public interface SearchEventRepository {

    SearchEvent getById(SearchEventId id);

    default SearchEvent getBySearchEventId(SearchEventId searchEventId) {
        return getById(searchEventId);
    }

    SearchEventId save(SearchEvent entity);

    List<SearchEvent> listByCreatedAtRange(Date createdAtStart, Date createdAtEnd);

    PageResult<SearchEvent> page(
            String queryText, String intentType, String searchStatus, String operatorId, int pageNo, int pageSize);
}
