package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;

public interface QueryUnderstandingRepository {

    QueryUnderstanding getBySearchEventId(String searchEventId);

    Long save(QueryUnderstanding entity);

    int update(QueryUnderstanding entity);
}
