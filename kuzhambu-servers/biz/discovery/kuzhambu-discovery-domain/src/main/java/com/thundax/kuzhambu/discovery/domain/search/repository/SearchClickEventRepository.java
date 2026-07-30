package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClickEvent;
import java.util.Date;

public interface SearchClickEventRepository {

    SearchClickEvent getById(Long id);

    default SearchClickEvent getBySearchClickEventId(String searchClickEventId) {
        return getById(searchClickEventId == null ? null : Long.valueOf(searchClickEventId));
    }

    Long save(SearchClickEvent entity);

    long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd);
}
