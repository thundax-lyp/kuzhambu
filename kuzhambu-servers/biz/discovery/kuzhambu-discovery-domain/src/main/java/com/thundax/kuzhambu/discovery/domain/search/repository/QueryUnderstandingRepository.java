package com.thundax.kuzhambu.discovery.domain.search.repository;

import com.thundax.kuzhambu.discovery.domain.search.model.entity.QueryUnderstanding;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.QueryUnderstandingId;
import com.thundax.kuzhambu.discovery.domain.search.model.valueobject.SearchEventId;

public interface QueryUnderstandingRepository {

    QueryUnderstanding getBySearchEventId(SearchEventId searchEventId);

    QueryUnderstandingId save(QueryUnderstanding entity);

    int update(QueryUnderstanding entity);
}
