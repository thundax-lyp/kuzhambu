package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.SearchClickEvent;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchClickEventId;
import java.util.Date;

public interface SearchClickEventRepository {

    SearchClickEvent getById(SearchClickEventId id);

    default SearchClickEvent getBySearchClickEventId(SearchClickEventId searchClickEventId) {
        return getById(searchClickEventId);
    }

    SearchClickEventId save(SearchClickEvent entity);

    long countByCreatedAtRange(Date createdAtStart, Date createdAtEnd);
}
