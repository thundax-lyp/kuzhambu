package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;

public interface QueryUnderstandingRepository {

    QueryUnderstanding getBySearchEventId(Long searchEventId);

    default QueryUnderstanding getBySearchEventId(String searchEventId) {
        return getBySearchEventId(searchEventId == null ? null : Long.valueOf(searchEventId));
    }

    Long save(QueryUnderstanding entity);

    int update(QueryUnderstanding entity);
}
